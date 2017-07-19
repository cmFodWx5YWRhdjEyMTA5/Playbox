
#include "mcc_generated_files/dac1.h"
#include "mcc_generated_files/pwm1.h"
#include "mcc_generated_files/pwm2.h"
#include "mcc_generated_files/pwm3.h"
#include "fishoutput.h"
#include "fishstate.h"
#include "mcc_generated_files/pin_manager.h"

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
    // which is 0-4095 instead of 0-1023 so divide by 4
    uint16_t dutyValue = (uint16_t) (FISH_State.potPosition / 4.0);
    PWM1_LoadDutyValue(dutyValue);
    PWM2_LoadDutyValue(dutyValue);
    PWM3_LoadDutyValue(dutyValue);
}

void FISHOUPUT_setClock(void)
{
    // show the current time on the clock
    uint8_t hours = (uint8_t) (FISH_State.miliseconds / (K_MSECONDSINHOUR * 1.0));
    // only doing 12 rather than 24 hour clock
    bool isPm = false;
    if (hours > 12) {
        hours -= 12;
        isPm = true;
    }
    switch(hours) {
        case 1 :
            IO_TM1_SetHigh();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 2 :
            IO_TM1_SetLow();
            IO_TM2_SetHigh();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 3 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetHigh();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 4 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetHigh();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 5 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetHigh();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 6 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetHigh();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 7 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetHigh();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 8 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetHigh();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 9 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetHigh();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 10 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetHigh();
            IO_TM11_SetLow();
            IO_TM12_SetLow();
            break;
        case 11 :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetHigh();
            IO_TM12_SetLow();
            break;
        case 12 :
        case 0 :
        default :
            IO_TM1_SetLow();
            IO_TM2_SetLow();
            IO_TM3_SetLow();
            IO_TM4_SetLow();
            IO_TM5_SetLow();
            IO_TM6_SetLow();
            IO_TM7_SetLow();
            IO_TM8_SetLow();
            IO_TM9_SetLow();
            IO_TM10_SetLow();
            IO_TM11_SetLow();
            IO_TM12_SetHigh();
            break;
    }
    // and set the AM / PM lights
    if (isPm) {
        // set the AM bit LOW and the PM bit HIGH
        IO_TMAM_SetLow();
        IO_TMPM_SetHigh();
    }
    else {
        // set the AM bit HIGH and the PM bit LOW
        IO_TMAM_SetHigh();
        IO_TMPM_SetLow();
    }
}
