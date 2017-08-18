
#include "mcc_generated_files/dac1.h"
#include "mcc_generated_files/pwm1.h"
#include "mcc_generated_files/pwm2.h"
#include "mcc_generated_files/pwm3.h"
#include "mcc_generated_files/pin_manager.h"

#include "fishoutput.h"
#include "fishstate.h"
#include "fishinput.h"
#include "rtc.h"

#include <stdio.h>

bool FISHOUTPUT_disableHeat = true;
uint8_t hourPreviousSet = 99;   // initially invalid to set first time

static const uint8_t ledValues[49][3] = {
    {0,	0,	0},
    {0,	0,	0},
    {0,	0,	0},
    {0,	0,	0},
    {0,	0,	0},
    {0,	0,	0},
    {0,	0,	0},
    {0,	0,	0},
    {10,0,	0},
    {25,0,	0},
    {45,0,	0},
    {66,0,	0},
    {85,0,	0},
    {98,0,	0},
    {100,0,	0},
    {98,0,	0},
    {85,0,	10},
    {66,0,	25},
    {45,0,	45},
    {25,0,	66},
    {10,0,	85},
    {0,	0,	95},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	0,	100},
    {0,	10,	100},
    {0,	25,	95},
    {0,	45,	85},
    {0,	66,	66},
    {0,	85,	45},
    {0,	98,	25},
    {0,	100,10},
    {0,	98,	0},
    {0,	85,	0},
    {0,	66,	0},
    {0,	45,	0},
    {0,	25,	0},
    {0,	10,	0},
    {0,	0,	0},
    {0,	0,	0},  
};

void FISHOUTPUT_Initialize(void)
{
    // initialize anything we need to here...
    FISH_State.isLightsOn = true;
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
        printf("Button press\r\n");
        // handled this, reset the flag
        FISH_State.isButtonPress = false;
        // toggle the lights on/off
        FISH_State.isLightsOn = !FISH_State.isLightsOn;
    }
    if (FISH_State.isLongButtonPress) {
        printf("Long Button press\r\n");
        // move the time forward an hour
        RTC_IncrementHour();
        // now we have changed the hour - update it straight away
        RTC_ReadTime();
        // and debug it
        RTC_Print();
        // and reset the flag
        FISH_State.isLongButtonPress = false;
        // and inform the input we dealt with this
        FISHINPUT_longButtonPressHandled();
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
    //uint16_t dutyValue = (uint16_t) (FISH_State.potPosition / 4.0);
    // calculate the index in the array we require
    uint8_t timeIndex = RTC_State.time_hours * 2;
    // if we have 30 mins or more, add one
    if (RTC_State.time_minutes >= 30) {
        ++timeIndex;
    }
    // now get the percentages we require
    uint16_t red =   ((uint16_t)ledValues[timeIndex][0] / 100.0 * 1023.0);
    uint16_t blue =  ((uint16_t)ledValues[timeIndex][1] / 100.0 * 1023.0);
    uint16_t white = ((uint16_t)ledValues[timeIndex][2] / 100.0 * 1023.0);
    //TODO - calculate the duty value to set these percentages
    PWM1_LoadDutyValue(red);
    PWM2_LoadDutyValue(blue);
    PWM3_LoadDutyValue(white);
}

void FISHOUPUT_setClock(void)
{
    // show the current time on the clock
    // only doing 12 rather than 24 hour clock
    bool isPm = false;
    uint8_t hour = RTC_State.time_hours;
    if (hour != hourPreviousSet) {
        // this is a change in time clear the old and set the new
        hourPreviousSet = hour;
        // get the hour in 12hr format
        while (hour > 12) {
            // counteract the 24 hour display by removing 12 and remembering PM
            hour -= 12;
            isPm = true;
        }
        // clear the previous time display
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
        IO_TM12_SetLow();
        IO_TMAM_SetLow();
        IO_TMPM_SetLow();
        // and set the new one
        if (hour == 1) {
            IO_TM1_SetHigh();
        }
        else if (hour == 2) {
            IO_TM2_SetHigh();
        }
        else if (hour == 3) {
            IO_TM3_SetHigh();
        }
        else if (hour == 4) {
            IO_TM4_SetHigh();
        }
        else if (hour == 5) {
            IO_TM5_SetHigh();
        }
        else if (hour == 6) {
            IO_TM6_SetHigh();
        }
        else if (hour == 7) {
            IO_TM7_SetHigh();
        }
        else if (hour == 8) {
            IO_TM8_SetHigh();
        }
        else if (hour == 9) {
            IO_TM9_SetHigh();
            // time to reset the lights off to always be true, bad for the
            // fish never to have lights!...
            FISH_State.isLightsOn = true;
        }
        else if (hour == 10) {
            IO_TM10_SetHigh();
        }
        else if (hour == 11) {
            IO_TM11_SetHigh();
        }
        else {//if (hour == 12) {
            IO_TM12_SetHigh();
        }
        if (isPm) {
            // set the PM bit HIGH
            IO_TMPM_SetHigh();
        }
        else {
            // set the AM bit HIGH
            IO_TMAM_SetHigh();
        }
    }
}
