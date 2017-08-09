
#include "mcc_generated_files/adc.h"
#include "mcc_generated_files/pin_manager.h"
#include "mcc_generated_files/eusart.h"

#include "fishinput.h"
#include "fishstate.h"
#include "rtc.h"

#include <stdio.h>

uint32_t button_pressed_time;
bool last_button_state;
bool button_state;
bool wasLongButtonPress;

#define K_RX_BUFFER_SIZE 64
unsigned char rxBuffer[K_RX_BUFFER_SIZE + 1];
int rxIndex = 0;
char rxCommand = '0';

void FISHINPUT_Initialize(void)
{
    // initialize anything we need to here...
    button_pressed_time = 0;
    last_button_state = false;
    button_state = false;
    wasLongButtonPress = false;
	rxBuffer[0] = 0;
	rxIndex = 0;
	rxCommand = '0';
    
    // setup the button
    IO_BTN_SetDigitalInput();
}

void FISHINPUT_process(void)
{   
    // get the chip temp for our interest
    FISHINPUT_getChipTemp();
    // if we are the slave then we want to get our state from the serial line
    if (FISH_State.isSlave) {
        FISHINPUT_getStateSerial();
    }
    else {
        // we want to get our state from sensors etc
        FISHINPUT_getHotPlateTemp();
        FISHINPUT_getWaterTemp();
        // for debugging we can get the POT position too
        FISHINPUT_getPotPosition();

        // read the state of the button to get short and long presses
        bool buttonReading = IO_BTN_GetValue() == 1;
        if (buttonReading == true) {
            // the button is down
            if (last_button_state == false) {
                // wasn't pressed, now it is
                button_state = buttonReading;
                button_pressed_time = FISH_State.milliseconds;
            }
            // the button is down, has it been down a while?
            if (FISH_State.milliseconds - button_pressed_time > K_LONGBUTTONPRESSTIME) {
                // this was held down for more than a second - this is a long-press
                FISH_State.isLongButtonPress = true;
            }
        }
        else if (last_button_state) {
            // button is released from being pressed, if not a long press
            // then this was a short press
            if (false == wasLongButtonPress &&
                FISH_State.milliseconds - button_pressed_time > K_SHORTBUTTONPRESSTIME &&
                FISH_State.milliseconds - button_pressed_time < K_LONGBUTTONPRESSTIME) {
                // this was enough to register a quick press, not a long press
                FISH_State.isButtonPress = true;
            }
            // released now, reset any long button press we might have picked up
            wasLongButtonPress = false;
        }
        last_button_state = buttonReading;

        //TODO send this state to the slave over serial
        //FISHINPUT_sendStateSerial();
    }
}

void FISHINPUT_longButtonPressHandled(void)
{
    // when the long button press is handled, reset the timer
    button_pressed_time = FISH_State.milliseconds;
    wasLongButtonPress = true;
}

void FISHINPUT_sendStateSerial(void)
{
    // send out the serial data to encapsulate the data we need to send
    EUSART_Write('{');
    EUSART_Write(RTC_State.time_hours);
    EUSART_Write(FISH_State.red);
    EUSART_Write(FISH_State.green);
    EUSART_Write(FISH_State.blue);
    EUSART_Write('}');
}

void FISHINPUT_getStateSerial(void)
{
    while(eusartRxCount!=0 && rxIndex < K_RX_BUFFER_SIZE) 
    {   
        unsigned char ch = EUSART_Read();  // read a byte for RX
    	// read in a value, add to the buffer
		if (ch == '{') {
			// starting, set the rx counter to the start of the buffer
			rxIndex = 0;
		}
		else if (ch == '}') {
			// ended, process this command
            printf("received: ");
            printf(rxBuffer);
            printf(" with rxIndex of %d\r\n", rxIndex);
            
            RTC_State.time_hours = rxBuffer[rxIndex - 4];
            FISH_State.red  = rxBuffer[rxIndex - 3];
            FISH_State.green = rxBuffer[rxIndex - 2];
            FISH_State.blue = rxBuffer[rxIndex - 1];
			
			// reset for any more data
			rxCommand = '0';
			// reset the buffer to start again, wipe out the processed data
			rxIndex = 0;
			rxBuffer[0] = '\0';
		}
		else {
			if (rxCommand == '0') {
				// the first char is the command, set this
				rxCommand = ch;
			}
			else {
				// put the value after the command into the buffer
				rxBuffer[rxIndex++] = ch;
				// and terminate it here for the time being
				rxBuffer[rxIndex] = '\0';
			}
		}
	}
	if (rxIndex >= K_RX_BUFFER_SIZE) {
		// there is no more room left in the buffer (maxed out with room for the terminating char)
		// wipe out all that data to ignore it
		rxIndex = 0;
		rxCommand = '\0';
		rxBuffer[0] = '\0';
	}
}

float FISHINPUT_getHotPlateTemp(void)
{
    // get the reading from the sensor - using the created PIN ID
#ifdef K_DEBUG_HPT
    // if we are debugging then use the POT instead of the actual sensor
    adc_result_t sensor = FISHINPUT_getPotPosition();
    printf("DEBUGGING HOT PLATE\r\n");
#else
    // use the actual sensor
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
    printf("DEBUGGING WATER\r\n");
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
