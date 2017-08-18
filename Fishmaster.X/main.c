
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
 * RA4 is GPIO to read the state of the button
 * RA5 is GPIO_Out for a LED for debugging on curiosity
 * RA0 is GPIO_In for a POT for debugging on curiosity
 * RA7 is the slave pin, power to this if you want it to listen for serial
 * 
 * RA2 is analog out for the DAC - connect to hot-plate
 * 
 * RB3, RB4, RB5, RB6, RC0, RC5 is 1-6 hours
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
 * MSSP
 * RC3 is SCL for I2C communications
 * RC4 is SDA for I2C communications
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
    FISH_State.isDemoMode = false;
}

void fishProcess()
{
    // perform the processing here, first handle the input from sensors
    FISHINPUT_process();
    // and then handle the output to displays etc
    FISHOUTPUT_process();
    //** VERY IMPORTANT THAT OUTPUT IS AFTER INPUT - FOR BUTTON PRESSES
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
#ifdef K_DEBUG
    uint32_t lastPrintTime = FISH_State.milliseconds;
#endif
    uint32_t lastOffsetTime = FISH_State.milliseconds;
    while(1) {
        // along with the RTC we have a rolling milliseconds and tick
        // counter we can use for rougher timing functions, update this now
        // calculate the time (seconds) from this
        FISHSTATE_calcTime();
        if (FISH_State.milliseconds - lastOffsetTime > 1000) {
            // a second has passed, read in the time
            if (FISH_State.isDemoMode) {
                // we are in demo mode, force time on artificially to advance the lighting etc
                RTC_State.time_minutes += 15;
                if (RTC_State.time_minutes >= 60) {
                    // too many minutes, roll back to zero
                    RTC_State.time_minutes = 0;
                    // and move on the hour instead
                    RTC_State.time_hours += 1;
                    if (RTC_State.time_hours >= 24) {
                        // too many hours, roll back to zero
                        RTC_State.time_hours = 0;
                    }
                }
            }
            else {
                // read the time from the clock
                RTC_ReadTime();
            }
            // and reset the timer for this functionality
            lastOffsetTime = FISH_State.milliseconds;
#ifdef K_DEBUG
            // and toggle the LED so we know we are running
            LED_Toggle();
            printf(".");
#endif
        }
#ifdef K_DEBUG
        // and we can print out state here for debugging
        if (FISH_State.milliseconds - lastPrintTime > 5000) {
            // print the state periodically for debugging purposes
            FISHSTATE_print();
            // reset the print time to print only periodically
            lastPrintTime = FISH_State.milliseconds;
        }
#endif
        // always process the tasks we want to process
        fishProcess();
    }
}

/**
 End of File
*/