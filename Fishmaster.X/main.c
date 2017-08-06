
#include "mcc_generated_files/mcc.h"
#include <stdio.h>
#include <stdlib.h>

// local includes
#include "fishinput.h"
#include "fishstate.h"
#include "fishoutput.h"
#include "rtc.h"

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
    RTC_Initialise();
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
    
    // now do the processing
    uint32_t lastRtcReadTime = FISH_State.milliseconds;
    uint32_t lastPrintTime = lastRtcReadTime;
    while(1) {
        // we want to get the time on a periodic counter, so do this now
        if (FISH_State.milliseconds - lastRtcReadTime > 500) {
            // update the time
            RTC_ReadTime();
            // reset the time counter
            lastRtcReadTime = FISH_State.milliseconds;
        }
        // along with the RTC we have a rolling milliseconds and tick
        // counter we can use for rougher timing functions, update this now
        // calculate the time (seconds) from this
        FISHSTATE_calcTime();
        // and we can print out state here for debugging
        if (FISH_State.milliseconds - lastPrintTime > 5000) {
            // enough time has passed that we want to know what is going on
            // print our time
            RTC_Print();
            // and the state
            FISHSTATE_print();
            // and toggle the LED so we know we are running without serial on
            LED_Toggle();
            // reset the print time to print only periodically
            lastPrintTime = FISH_State.milliseconds;
        }
        // always process the tasks we want to process
        fishProcess();
    }
}

/**
 End of File
*/