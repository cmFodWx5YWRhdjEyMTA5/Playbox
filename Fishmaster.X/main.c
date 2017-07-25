
#include "mcc_generated_files/mcc.h"
#include <stdio.h>
#include <stdlib.h>

// local includes
#include "fishinput.h"
#include "fishstate.h"
#include "fishoutput.h"

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

void timer0Interrupt(void) {
    FISH_State.tick_count += 4864;
    //printf("%d/r/n", ((uint32_t) (TMR0_ReadTimer() * 1000)));
    //TMR0_Initialize();
}

void main(void)
{
    // initialize the device
    SYSTEM_Initialize();
    
      //Define interrupt Handlers
    TMR0_SetInterruptHandler (timer0Interrupt);

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
    
    // now do the processing
    uint32_t lastPrintTime = FISH_State.milliseconds;
    while(1) {
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