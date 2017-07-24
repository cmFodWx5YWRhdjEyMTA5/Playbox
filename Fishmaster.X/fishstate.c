
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
            (int)(FISH_State.miliseconds / 1000.0),
            FISH_State.potPosition, 
            ((int)FISH_State.chipTemp),
            ((int)FISH_State.hotPlateTemp),
            ((int)FISH_State.waterTemp),
            FISH_State.hotPlatePower);
    //TODO: printf passing %3.2f doesn't complile - grr
}

void FISHSTATE_calcTime(void)
{
    // calculate the time elapsed from the tick_count.
    // we are counting 2.56ms as 256 each time so there
    // will be 100 ticks per ms, we are counting separately
    // so we don't lose that .56, they will add up to 1 at some
    // point and we want to count that (O;
    while (FISH_State.tick_count > K_TICKSPERMSECOND) {
        // this has a second, remove it
        FISH_State.tick_count -= K_TICKSPERMSECOND;
        // and increment the number of seconds
        ++FISH_State.miliseconds;
    }
    while (FISH_State.miliseconds > K_MSECONDSINDAY) {
        // we have overflowed a day, just need to know the time
        // in the day, so remove the day in seconds
        FISH_State.miliseconds -= K_MSECONDSINDAY;
    }
    // update the hour displayed for this
    FISH_State.hour = (uint8_t) (FISH_State.miliseconds / (K_MSECONDSINHOUR * 1.0));
}
    
