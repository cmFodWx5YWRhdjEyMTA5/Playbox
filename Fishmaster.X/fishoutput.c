
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
#include <pic16f1787.h>

bool FISHOUTPUT_disableHeat = true;
uint8_t hourPreviousSet = 99;   // start off with invalid value to set first time

unsigned char clockStates[] = {
    0b01111111,     // midnight, set COM to be HIGH, PM to LOW and all others HIGH
    0b01000000,     // 1 - COM to LOW - 1-2 HIGH to be on and all others LOW
    0b10111111,     // 2 - COM to HIGH - 1-2 LOW to be on and all others HIGH
    0b00100000,     // 3 - COM to LOW - 3-4 HIGH to be on and all others LOW
    0b11011111,     // 4 - COM to HIGH - 3-4 LOW to be on and all others HIGH
    0b00010000,     // 5 - COM to LOW - 5-6 HIGH to be on and all others LOW
    0b11101111,     // 6 - COM to HIGH - 5-6 LOW to be on and all others HIGH
    0b00001000,     // 7 - COM to LOW - 7-8 HIGH to be on and all others LOW
    0b11110111,     // 8 - COM to HIGH - 7-8 LOW to be on and all others HIGH
    0b00000100,     // 9 - COM to LOW - 9-10 HIGH to be on and all others LOW
    0b11111011,     // 10- COM to HIGH - 9-10 LOW to be on and all others HIGH
    0b00000010,     // 11- COM to LOW - 11 HIGH to be on and all others LOW
    0b10000000      // mid-day, set COM to be LOW, PM to HIGH and all others LOW
};

void FISHOUTPUT_Initialize(void)
{
    // initialize anything we need to here...
    FISH_State.isLightsOn = true;
    DAC1_Initialize();
}

void FISHOUTPUT_process(void)
{
    if (FISH_State.waterTemp < K_TEMPLOWERLIMIT || FISH_State.hotPlateTemp < K_TEMPLOWERLIMIT) {
        // the sensors are not reading right, do nothing with this else we could set fire to something
        FISHOUTPUT_setHotPlatePower(0);
    }
    else {
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
    }
    if (!FISH_State.isDemoMode) {
        // we are not in demo mode, process this button press
        if (FISH_State.isButtonPress) {
            printf("Button press\r\n");
            // handled this, reset the flag
            FISH_State.isButtonPress = false;
            // toggle the lights on/off
            FISH_State.isLightsOn = FISH_State.isLightsOn == false;
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
        //N.B. doing 150 to limit to 3V on the max
        uint8_t powerOutput = (uint8_t)(255.0 * (powerPercent / 100.0));
        // set this on the state
        FISH_State.hotPlatePower = powerOutput;
        // set this on the DAC to output this voltage now
        DAC1_SetOutput(powerOutput);
    }
    else {
        // disable the hot-plate and quick - it is too hot )O;
        DAC1_SetOutput(0);
        // set this on the state
        FISH_State.hotPlatePower = 0;
    }
}

float FISHOUTPUT_gaussianValue(float time, float peak, float std, float max)
{
    // apply the gaussian formulae
    float value = max - (((time-peak) * (time-peak)) / (2.0 * (std * std)));
    //TODO reduce the intensity % based on the developer's preset input
    
    // and return limited to the max value of 0 to stop negative values and 250
    // to prevent overflowing the PWM
    return MAX(0.0, MIN(250.0, value));
}

void FISHOUTPUT_setLighting(void)
{
    //http://embedded-lab.com/blog/lab-9-pulse-width-modulation-pwm/
    // now get the percentages we require
    // CCP module uses the 8 MSBs to set the duty value so 0-255
#ifdef K_DEBUG_LED
    // for debugging set the LEDs based on the position of the POT
    // which is 0-4095 
    red = ((uint16_t)FISH_State.potPosition / 4095.0 * 250.0);
    blue = ((uint16_t)FISH_State.potPosition / 4095.0 * 250.0);
    white = ((uint16_t)FISH_State.potPosition / 4095.0 * 250.0);
#endif
    if (FISH_State.isLightsOn) {
        // the value for time needs to be a decimal value of hours
        float timeHrs = RTC_State.time_hours + (RTC_State.time_minutes / 60.0);
        // the formula is simple, we are using a gaussian curve...
        uint16_t value =   (uint16_t)FISHOUTPUT_gaussianValue(
                timeHrs,
                K_GAUSSIAN_RED_PEAK,
                K_GAUSSIAN_RED_STD,
                K_GAUSSIAN_RED_MAX);
        // PWM on the CCP module uses only the 8 MSBs of the CCPCON so 0-255 << 2
        PWM2_LoadDutyValue(value << 2);
        // now do blue
        value = (uint16_t)FISHOUTPUT_gaussianValue(
                timeHrs,
                K_GAUSSIAN_BLUE_PEAK,
                K_GAUSSIAN_BLUE_STD,
                K_GAUSSIAN_BLUE_MAX);
        // PWM on the CCP module uses only the 8 MSBs of the CCPCON so 0-255 << 2
        PWM1_LoadDutyValue(value << 2);
        // and white
        value = (uint16_t)FISHOUTPUT_gaussianValue(
                timeHrs,
                K_GAUSSIAN_WHITE_PEAK,
                K_GAUSSIAN_WHITE_STD,
                K_GAUSSIAN_WHITE_MAX);
        // PWM on the CCP module uses only the 8 MSBs of the CCPCON so 0-255 << 2
        PWM3_LoadDutyValue(value << 2);
    }
    else {
        // turn off the lights
        PWM1_LoadDutyValue(0);
        PWM2_LoadDutyValue(0);
        PWM3_LoadDutyValue(0);
    }
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
        
        // PORTS are all on D and as follows:
        // PM, 1-2, 3-4, 5-6 7-8, 9-10, 11, COM
        // so set the state all in one go
        PORTD = clockStates[hour];
    }
}
