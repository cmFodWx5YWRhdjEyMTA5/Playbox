
#include "mcc_generated_files/dac1.h"
//#include "mcc_generated_files/pwm1.h"
//#include "mcc_generated_files/pwm2.h"
//#include "mcc_generated_files/pwm3.h"
#include "fishoutput.h"
#include "fishstate.h"

bool FISHOUTPUT_disableHeat = true;

void FISHOUTPUT_Initialize(void)
{
    // initialize anything we need to here...
}

void FISHOUTPUT_process(void)
{
    // there is no real processing here but there might be...
}

void FISHOUTPUT_setHotPlatePower(uint8_t powerPercent)
{
    // set the power for the hot-plate here, will be called when the temp
    // of the water is too low so will be high when very cold, lower when
    // closer and off when just right - ahhh, Goldilocks
    
    // let's check the temperature then
    if (FISH_State.hotPlateTemp > K_MAX_HOTPLATETEMP) {
        // this is too hot, bad bear.
        FISHOUTPUT_disableHeat = true;
    }
    else if (FISHOUTPUT_disableHeat &&
             FISH_State.hotPlateTemp < K_MIN_HOTPLATETEMPTORESTART) {
        // ahh, cool enough now having been disabled, re-enable
        FISHOUTPUT_disableHeat = false;
    }
    
    // now let's see about setting that temp just right
    if (false == FISHOUTPUT_disableHeat) {
        // we are enabled, set the output on the DAC
        // the DAC goes from 0 - 255 so get the value as a percentage now
        uint8_t powerOutput = (uint8_t)(255.0 * (powerPercent / 100.0));
        // set this on the state
        FISH_State.hotPlatePower = powerPercent;
        // set this on the DAC to output this voltage now
        DAC1_SetOutput(powerOutput);
    }
    else {
        // disable the hot-plate and quick - it is too hot )O;
        DAC1_SetOutput(0);
        // set this on the state
        FISH_State.hotPlatePower = powerPercent;
    }
}

void FISHOUTPUT_setLighting(void)
{
    // For now just set the LEDs based on the position of the POT
    //PWM1_LoadDutyValue(FISH_State.potPosition);
    //PWM2_LoadDutyValue(FISH_State.potPosition);
    //PWM3_LoadDutyValue(FISH_State.potPosition);
}
