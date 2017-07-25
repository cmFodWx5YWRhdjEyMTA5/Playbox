
#include "fishstate.h"
#include <stdio.h>
#include <stdlib.h>

struct t_fishstate FISH_State;

#define K_TICKSPERMSECOND 100

void FISHSTATE_print(void)
{
    // print out all the members of the global struct that stores our state
    // mostly for debugging purposes
    printf("S:%5d POT: %d, CHIP: %d, HotPlate: %d, Water: %d, HPPower: %d\r\n", 
            (int)(FISH_State.milliseconds / 1000.0),
            FISH_State.potPosition, 
            ((int)FISH_State.chipTemp),
            ((int)FISH_State.hotPlateTemp),
            ((int)FISH_State.waterTemp),
            FISH_State.hotPlatePower);
    //TODO: printf passing %3.2f doesn't complile - grr
}

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
    
    // do the day roll over too, this is more straightforward as only expecting
    // one at a time, do it in a lazy loop instead of with maths.
    while (FISH_State.milliseconds > K_MSECONDSINDAY) {
        // we have overflowed a day, just need to know the time
        // in the day, so remove the day in seconds
        FISH_State.milliseconds -= K_MSECONDSINDAY;
    }
    // update the hour displayed for this
    FISH_State.hour = (uint8_t) (FISH_State.milliseconds / (K_MSECONDSINHOUR * 1.0));
    
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
    
