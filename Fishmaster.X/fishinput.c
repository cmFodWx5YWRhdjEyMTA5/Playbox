
#include "mcc_generated_files/adc.h"
#include "fishinput.h"
#include "fishstate.h"

void FISHINPUT_Initialize(void)
{
    // initialize anything we need to here...
}

void FISHINPUT_process(void)
{
    // there is no real processing here but for interest we like to know
    // the position of the potentiometer on the programming board and
    // the internal chip temperature, calling these functions will set them
    // on the state which might be of interest to us later
    FISHINPUT_getChipTemp();
    FISHINPUT_getPotPosition();
}
    
bool FISHINPUT_isPressed(void)
{
    return false;
}

bool FISHINPUT_isLongPressed(void)
{
    return false;
}

float FISHINPUT_getHotPlateTemp(void)
{
    // get the reading from the sensor - using the created PIN ID
#ifdef K_DEBUG_HPT
    // if we are debugging then use the POT instead of the actual sensor
    adc_result_t sensor = FISHINPUT_getPotPosition();
#else
    // use he actual sensor
    adc_result_t sensor = ADC_GetConversion(channel_HPT);
#endif

    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.hotPlateTemp = temp;
    // and return the result
    return temp;
}

float FISHINPUT_getWaterTemp(void)
{
    // get the reading from the sensor - using the created PIN ID
#ifdef K_DEBUG_WT
    // if we are debugging then use the POT instead of the actual sensor
    adc_result_t sensor = FISHINPUT_getPotPosition();
#else
    // use he actual sensor
    adc_result_t sensor = ADC_GetConversion(channel_WT);
#endif

    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.waterTemp = temp;
    // and return the result
    return temp;
}

uint16_t FISHINPUT_getPotPosition(void)
{
    // get the reading
    adc_result_t sensor = ADC_GetConversion(channel_POT);
    // set it in the state
    FISH_State.potPosition = sensor;
    // and return it
    return sensor;
}

float FISHINPUT_getChipTemp(void)
{
    // get the reading from the internal sensor - just because we can (O:
    adc_result_t sensor = ADC_GetConversion(channel_Temperature);
    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.chipTemp = temp;
    // and return the result
    return temp;
}
