
#include "mcc_generated_files/dac1.h"
#include "mcc_generated_files/pwm1.h"
#include "mcc_generated_files/pwm2.h"
#include "mcc_generated_files/pwm3.h"
#include "mcc_generated_files/pin_manager.h"

#include "fishoutput.h"
#include "fishstate.h"

#include <stdio.h>

bool FISHOUTPUT_disableHeat = true;

void FISHOUTPUT_Initialize(void)
{
    // initialize anything we need to here...
}

void FISHOUTPUT_process(void)
{
    // now we want to set the temperature of the hot-plate
    float tempDifferential = K_TARGETWATERTEMP - FISH_State.waterTemp;
    if (tempDifferential > K_TARGETWATERTEMPTHREHOLD) {
        // we are below temp, let's get this differential as a percentage
        // of our target temp (using 500% of the target temp to get their quicker when really cold)
        uint8_t power = (uint8_t)MIN(tempDifferential / K_TARGETWATERTEMP * 500.0, 100.0);
        // now this is a percentage, let's set the power level of the hot-plate
        FISHOUTPUT_setHotPlatePower(power);
    }
    else {
        // we are fine either at temp or unfortunately too hot, 
        // either way just turn the hot-plate off
        FISHOUTPUT_setHotPlatePower(0);
    }
    // process this button press
    if (FISH_State.isButtonPress) {
        // move the time forward an hour
        FISH_State.miliseconds += K_MSECONDSINHOUR;
        // handled this, reset the flag
        FISH_State.isButtonPress = false;
        // calc time to fix any overrun
        FISHSTATE_calcTime();
        // and debug
        printf("Moved time forward an hour to %d\r\n", FISH_State.hour);
    }
    if (FISH_State.isLongButtonPress) {
        //TODO handle the long press for something...
        printf("That was a looooong, press\r\n");
        FISH_State.isLongButtonPress = false;
    }
    // set the lighting correctly
    FISHOUTPUT_setLighting();
    // and the clock
    FISHOUPUT_setClock();
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
    //TODO use the R, G, B values from the state we have received / calculated
    PWM1_LoadDutyValue(dutyValue);
    PWM2_LoadDutyValue(dutyValue);
    PWM3_LoadDutyValue(dutyValue);
}

void FISHOUPUT_setClock(void)
{
    // show the current time on the clock
    // only doing 12 rather than 24 hour clock
    bool isPm = false;
    while (FISH_State.hour > 12) {
        FISH_State.hour -= 12;
        isPm = true;
    }
    switch(FISH_State.hour) {
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
