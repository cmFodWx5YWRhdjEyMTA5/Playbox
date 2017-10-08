
#include "mcc_generated_files/mcc.h"
#include <stdio.h>
#include <stdlib.h>

// local includes
#include "fishinput.h"
#include "fishstate.h"
#include "fishoutput.h"
#include "rtc.h"

/*                      Main application
 * 
 * DEV SITE: http://microchipdeveloper.com/8bit:interrupts
 * 
 */

void fishInitialise(void)
{
    // output the startup state on the serial port for debugging
    printf("\rFishMaster controller application - created %s %s\r\n",__DATE__, __TIME__);
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
        if (FISH_State.milliseconds - lastOffsetTime > 100) {
            // a tenth of a second has passed, read in the time
            if (FISH_State.isDemoMode) {
                // we are in demo mode, force time on artificially to advance the lighting etc
                RTC_State.time_minutes += 1;
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
            printf(".");
#endif
        }
#ifdef K_DEBUG
        // and we can print out state here for debugging
        if (FISH_State.milliseconds - lastPrintTime > 5000) {
            // print the state periodically for debugging purposes
            FISHSTATE_print();
            // and toggle the LED so we know we are running
            IO_PWR_Toggle();
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