
#include "fishstate.h"
#include "rtc.h"
#include <stdio.h>
#include <stdlib.h>

struct t_fishstate FISH_State;

#define K_TICKSPERMSECOND 100

#ifdef K_DEBUG
void FISHSTATE_print(void)
{
    // print out all the members of the global struct that stores our state
    // mostly for debugging purposes
    
    printf("\r\n%s: %.2d:%.2d:%.2d POT: %.4d, Chip: %.3d, HotPlate: %.3d, Water: %.3d, Intensity: %.3d, HPPower: %.3d\r\n",
            FISH_State.isDemoMode ? "DEMO" : "TIME",
            RTC_State.time_hours,
            RTC_State.time_minutes,
            RTC_State.time_seconds,
#if defined(K_DEBUG_WT) || defined(K_DEBUG_HPT)
            FISH_State.potPosition, 
#else
            0,
#endif
#ifdef K_DEBUG
            FISH_State.chipTemp,
#else
            0,
#endif
            ((uint16_t)FISH_State.hotPlateTemp),
            ((uint16_t)FISH_State.waterTemp),
            ((uint16_t)FISH_State.intensity),
            FISH_State.hotPlatePower);
    //TODO: printf passing %3.2f doesn't complile - grr
}
#endif
void FISHSTATE_calcTime(void)
{
    // calculate the time elapsed from the tick_count which is stored in 1/100
    // of a ms to keep the 2dp it is triggered in
    
    // do this quick as the interrupt could occur at any time and change it
    uint32_t ticks = FISH_State.tick_count;
    FISH_State.tick_count = 0;
    // now let's see how many milliseconds that was
    uint32_t msecs = (uint32_t) (ticks / (K_TICKSPERMSECOND * 1.0));
    // put the left over back on the counter
    FISH_State.tick_count += ticks - (msecs * K_TICKSPERMSECOND);
    // but add the actual milliseconds to our counter
    FISH_State.milliseconds += msecs;
    
    /* TODO: Why doesn't this compile )O; FULL CODE?
     div_t output;

    output = div(FISH_State.tick_count, K_TICKSPERMSECOND);
    // the tick_count should contain the remainder
    FISH_State.tick_count = output.rem;
    // and we can add the quotient to our timer
    FISH_State.milliseconds += output.quot;
    
    // we have milliseconds now, do the day too while we are here
    output = div(FISH_State.milliseconds, K_MSECONDSINDAY);
    // milliseconds is the remainder
    FISH_State.milliseconds = output.rem;
    // and we can ignore the quotient - will have rolled over a number of days
    
    // but let's keep track of the hour we are in
    output = div(FISH_State.milliseconds, K_MSECONDSINHOUR);
    // update the hour displayed for this
    FISH_State.hour = output.quot;*/
}