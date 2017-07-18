
#include "fishstate.h"
#include <stdio.h>
#include <stdlib.h>

struct t_fishstate FISH_State;

void FISHSTATE_print(void)
{
    // print out all the members of the global struct that stores our state
    // mostly for debugging purposes
    printf("POT: %d, CHIP: %d, HotPlate: %d, Water: %d, HPPower: %d\r\n", 
            FISH_State.potPosition, 
            ((int)FISH_State.chipTemp),
            ((int)FISH_State.hotPlateTemp),
            ((int)FISH_State.waterTemp),
            FISH_State.hotPlatePower);
    //TODO: printf passing %3.2f doesn't complile - grr
}
