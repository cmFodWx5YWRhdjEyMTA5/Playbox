
#include "mcc_generated_files/mcc.h"
#include <stdio.h>
#include <stdlib.h>

// local includes
#include "fishinput.h"
#include "fishstate.h"
#include "fishoutput.h"

#define MCP79410_RETRY_MAX  100  // define the retry count
#define MCP79410_ADDRESS    0b1101111 // RTC device address

/*
                         Main application
 * 
 * WIRING:
 * 3V3 to positive pin of temp sensor (1)
 * GND to negative pin of temp sensor (3)
 * RB0 is AN_12 to read pin of hotplate temp sensor (2)
 * RB1 is AN_10 to read pin of water temp sensor (2)
 * RB2 is GPIO to read the state of the button
 * RA7 is the slave pin, power to this if you want it to listen for serial
 * 
 * RB7 is analog out for the DAC - connect to hot-plate
 * 
 * RA4, RA6, RC0, RC3, RC4, RC5 is 1-6 hours
 * RD0-RD5 is 7-12 hours
 * RD6 is AM
 * RD7 is PM
 * 
 * RC2 is CCP1 on TMR2 for G LED Lighting
 * RC1 is CCP2 on TMR2 for R LED Lighting
 * RE0 is CCP3 on TMR2 for B LED Lighting
 * 
 * SERIAL-OUT
 * RC7 orange TTL-232RG wire
 * RC6 yellow TTL-232RG wire
 * 3V3 red TTL-232RG wire
 * GND black TTL-232RG wire
 * 
 */

void fishInitialise(void)
{
    //output the startup state on the serial port for debugging
    printf("\rFishMaster controller application - %s %s\r\n",__DATE__, __TIME__);
    // do the local initialization
    FISHINPUT_Initialize();
    FISHOUTPUT_Initialize();
    // initialize the time from now
    FISH_State.tick_count = 0;
    FISH_State.milliseconds = 0;
    
    //TODO set the master/slave switch
    FISH_State.isSlave = false;//IO_SLV_GetValue() == 0;
    FISH_State.isSlave ? printf("IS SLAVE\r\n") : printf("IS MASTER\r\n");
    
    
}

void fishProcess()
{
    // perform the processing here, first handle the input from sensors
    FISHINPUT_process();
    // and then handle the output to displays etc
    FISHOUTPUT_process();
}

void timer2Interrupt(void) {
    FISH_State.tick_count += 816; // 816 is the callback function rate
    //TMR0_Initialize();
}

void rtcRead(void)
{   
    uint8_t     writeBuffer[2];
    writeBuffer[0] = 0xFF;
    writeBuffer[1] = 0b11011111;
    uint8_t     data;

    // Now it is possible that the slave device will be slow.
    // As a work around on these slaves, the application can
    // retry sending the transaction
    uint16_t timeOut = 0;
    I2C_MESSAGE_STATUS status;
    while(status != I2C_MESSAGE_FAIL)
    {
        // write the data on the I2C channel to request the time data
        I2C_MasterWrite(&writeBuffer, 1, MCP79410_ADDRESS, &status);

        // wait for the message to be sent or status has changed.
        while(status == I2C_MESSAGE_PENDING);

        if (status == I2C_MESSAGE_COMPLETE)
            break;

        // if status is  I2C_MESSAGE_ADDRESS_NO_ACK,
        //               or I2C_DATA_NO_ACK,
        // The device may be busy and needs more time for the last
        // write so we can retry writing the data, this is why we
        // use a while loop here

        // check for max retry and skip this byte
        if (timeOut == MCP79410_RETRY_MAX)
            break;
        else
            timeOut++;
    }

    if (status == I2C_MESSAGE_COMPLETE)
    {

        // this portion will read the byte from the memory location.
        timeOut = 0;
        for (int i = 0; i < 1; ++i) {
            // read in the 3 expected items of data
            while(status != I2C_MESSAGE_FAIL)
            {
                // read the data from the RTC one at a time
                I2C_MasterRead(&data, 1, MCP79410_ADDRESS, &status);

                // wait for the message to be sent or status has changed.
                while(status == I2C_MESSAGE_PENDING);

                if (status == I2C_MESSAGE_COMPLETE) {
                    printf("Data received: %d\r\n", data);
                    break;
                }

                // if status is  I2C_MESSAGE_ADDRESS_NO_ACK,
                //               or I2C_DATA_NO_ACK,
                // The device may be busy and needs more time for the last
                // write so we can retry writing the data, this is why we
                // use a while loop here

                // check for max retry and skip this byte
                if (timeOut == MCP79410_RETRY_MAX)
                    break;
                else
                    timeOut++;
            }

            // exit if the last transaction failed
            if (status == I2C_MESSAGE_FAIL)
            {
                break;
            }
        }
    }
}

void main(void)
{
    // initialize the device
    SYSTEM_Initialize();
    
    //Define interrupt Handlers
    TMR2_SetInterruptHandler (timer2Interrupt);

    // When using interrupts, you need to set the Global and Peripheral Interrupt Enable bits
    // Use the following macros to:

    // Enable the Global Interrupts
    INTERRUPT_GlobalInterruptEnable();

    // Enable the Peripheral Interrupts
    INTERRUPT_PeripheralInterruptEnable();

    // Disable the Global Interrupts
    //INTERRUPT_GlobalInterruptDisable();

    // Disable the Peripheral Interrupts
    //INTERRUPT_PeripheralInterruptDisable();
    
    // do the local initialization
    fishInitialise();

    //PORT RC4 appears to be the SDA line, enable the internal pull up resistor
    /*RC
    nRBPU = 0;                    //Enable PORTB internal pull up resistor
    TRISB = 0xFF;                 //PORTB as input
    I2C_Master_Init(100000);      //Initialize I2C Master with 100KHz clock
    while(1)
    {
        // start the I2C communications
        I2C_Master_Start();
        // send the control byte to the clock (which is 7-bits only) 1-read 0-write
        I2C_Master_Write(0b11011111);
        // now send the address we want to read (0x0 is seconds please)
        I2C_Master_Write(0x0);
        // now we can read the seconds
        unsigned short seconds = I2C_Master_Read(0); //Read + Acknowledge
        printf("Seconds: %d\r\n", seconds);
        I2C_Master_Stop();          //Stop condition
        __delay_ms(200);
    }*/
    
    // now do the processing
    uint32_t lastPrintTime = FISH_State.milliseconds;
    while(1) {
        rtcRead();
        // reset the time counter so we don't overflow
        //
        /*if (TMR0_HasOverflowOccured()) {
            // add the time interval to the tick (2.56ms)
            FISH_State.tick_count += 256;
            // and reset the timer
            TMR0_Initialize();
        }*/
        // calculate the time (seconds) from this
        FISHSTATE_calcTime();
        // and we can print out state here for debugging
        if (FISH_State.milliseconds - lastPrintTime > 5000) {
            // a second has passed, for debugging print out our state
            FISHSTATE_print();
            LED_Toggle();
            lastPrintTime = FISH_State.milliseconds;
        }
        // and process the tasks we want to process
        fishProcess();
    }
}

/**
 End of File
*/