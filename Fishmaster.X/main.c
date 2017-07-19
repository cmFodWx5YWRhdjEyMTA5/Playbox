/**
  Generated Main Source File

  Company:
    Microchip Technology Inc.

  File Name:
    main.c

  Summary:
    This is the main file generated using PIC10 / PIC12 / PIC16 / PIC18 MCUs 

  Description:
    This header file provides implementations for driver APIs for all modules selected in the GUI.
    Generation Information :
        Product Revision  :  PIC10 / PIC12 / PIC16 / PIC18 MCUs  - 1.45
        Device            :  PIC16F1784
        Driver Version    :  2.00
    The generated drivers are tested against the following:
        Compiler          :  XC8 1.35
        MPLAB             :  MPLAB X 3.40
*/

/*
    (c) 2016 Microchip Technology Inc. and its subsidiaries. You may use this
    software and any derivatives exclusively with Microchip products.

    THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS". NO WARRANTIES, WHETHER
    EXPRESS, IMPLIED OR STATUTORY, APPLY TO THIS SOFTWARE, INCLUDING ANY IMPLIED
    WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A
    PARTICULAR PURPOSE, OR ITS INTERACTION WITH MICROCHIP PRODUCTS, COMBINATION
    WITH ANY OTHER PRODUCTS, OR USE IN ANY APPLICATION.

    IN NO EVENT WILL MICROCHIP BE LIABLE FOR ANY INDIRECT, SPECIAL, PUNITIVE,
    INCIDENTAL OR CONSEQUENTIAL LOSS, DAMAGE, COST OR EXPENSE OF ANY KIND
    WHATSOEVER RELATED TO THE SOFTWARE, HOWEVER CAUSED, EVEN IF MICROCHIP HAS
    BEEN ADVISED OF THE POSSIBILITY OR THE DAMAGES ARE FORESEEABLE. TO THE
    FULLEST EXTENT ALLOWED BY LAW, MICROCHIP'S TOTAL LIABILITY ON ALL CLAIMS IN
    ANY WAY RELATED TO THIS SOFTWARE WILL NOT EXCEED THE AMOUNT OF FEES, IF ANY,
    THAT YOU HAVE PAID DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.

    MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE
    TERMS.
*/

#include "mcc_generated_files/mcc.h"
#include <stdio.h>
#include <stdlib.h>

// local includes
#include "fishinput.h"
#include "fishstate.h"
#include "fishoutput.h"

#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))


/*
                         Main application
 * 
 * WIRING:
 * 3V3 to positive pin of temp sensor (1)
 * GND to negative pin of temp sensor (3)
 * RB0 is AN_12 to read pin of hotplate temp sensor (2)
 * RB1 is AN_10 to read pin of water temp sensor (2)
 * RB2 is GPIO to read the state of the button
 * 
 * RB7 is analog out for the DAC - connect to hot-plate
 * 
 * RA4, RA6, RC0, RC3, RC4, RC5 is 1-6 hours
 * RD0-RD5 is 7-12 hours
 * RD6 is AM
 * RD7 is PM
 * 
 * RC2 is CCP1 on TMR2 for G LED Lighting
 * RC1 is CCP2 on TMR2 for R LED Lighting
 * RE0 is CCP3 on TMR2 for B LED Lighting
 * 
 * SERIAL-OUT
 * RC7 orange TTL-232RG wire
 * RC6 yellow TTL-232RG wire
 * 3V3 red TTL-232RG wire
 * GND black TTL-232RG wire
 * 
 */
    
/**
 * Do serial send and receive (MASTER / SLAVE header?)
 * 
 * Diagram PINS
 * Comment up and document - maybe tests?
 */

void fishInitialise(void)
{
    //output the startup state on the serial port for debugging
    printf("\rFishMaster controller application - Date 18/07/2017\r\n");  
    printf("UART Communications 8-bit Rx and Tx\r\n\n");
    
    // do the local initialization
    FISHINPUT_Initialize();
    FISHOUTPUT_Initialize();
    // initialise the time from now
    FISH_State.tick_count = 0;
    FISH_State.miliseconds = 0;
}

void fishProcess()
{
    // perform the processing here
    FISHINPUT_process();
    FISHOUTPUT_process();
    
    // get our data - this will also update our state
    FISHINPUT_getHotPlateTemp();
    
    // now we want to set the temperature of the hot-plate
    float tempDifferential = K_TARGETWATERTEMP - FISHINPUT_getWaterTemp();
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
    // set the lighting correctly
    FISHOUTPUT_setLighting();
    // and the clock
    FISHOUPUT_setClock();

    if(eusartRxCount!=0) 
    {   
        unsigned char readChar=EUSART_Read();  // read a byte for RX

        EUSART_Write(readChar);  // send a byte to TX  (from Rx)

        switch(readChar)    // check command  
        {
         case 'H':
         case 'h':
            {
                LED_SetHigh();
                printf(" -> LED On!!      \r");             
                break;
            }
         case 'L':
         case 'l':
            {
                LED_SetLow();
                printf(" -> LED Off!!     \r");   
                break;
            }
         default:
            {
                printf(" -> Fail Command!! \r");            
                break;
            }       
        }
    }
}

void main(void)
{
    // initialize the device
    SYSTEM_Initialize();

    // When using interrupts, you need to set the Global and Peripheral Interrupt Enable bits
    // Use the following macros to:

    // Enable the Global Interrupts
    INTERRUPT_GlobalInterruptEnable();

    // Enable the Peripheral Interrupts
    INTERRUPT_PeripheralInterruptEnable();

    // Disable the Global Interrupts
    //INTERRUPT_GlobalInterruptDisable();

    // Disable the Peripheral Interrupts
    //INTERRUPT_PeripheralInterruptDisable();
    
    // do the local initialization
    fishInitialise();
    
    // now do the processing
    uint32_t lastPrintTime = FISH_State.miliseconds;
    while(1) {
        FISH_State.tick_count += TMR2_ReadTimer();
        //if (TMR2_HasOverflowOccured()) {
        //    FISH_State.tick_count += 256;
        //}
        // calculate the time (seconds) from this
        FISHSTATE_calcTime();
        // and we can print out state here for debugging
        if (FISH_State.miliseconds - lastPrintTime > 5000) {
            // a second has passed, for debugging print out our state
            FISHSTATE_print();
            LED_Toggle();
            lastPrintTime = FISH_State.miliseconds;
        }
        // and process the tasks we want to process
        fishProcess();
        // and print out our state
        //FISHSTATE_print();
    }
}

/**
 End of File
*/