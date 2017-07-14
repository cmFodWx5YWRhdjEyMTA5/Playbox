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

/*
                         Main application
 * 
 * WIRING:
 * 3V3 to positive pin of temp sensor (1)
 * GND to negative pin of temp sensor (3)
 * RB0 to read pin of temp sensor (2)
 * 
 * RC7 orange TTL-232RG wire
 * RC6 yellow TTL-232RG wire
 * 3V3 red TTL-232RG wire
 * GND black TTL-232RG wire
 * 
 * RB7 is analogue out for the DAC
 */

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
    
    printf("\rPICDEM LAB II - Date 08/11/2015\r\n");  
    printf("UART Communications 8-bit Rx and Tx\r\n\n");
    printf("Keyboard Type H : LED ON   Type L: LED OFF \r\n\n");
    
    while (1)
    {
        // Add your application code
        adc_result_t pot = ADC_GetConversion(channel_POT);
        //printf("POT AT:");
        //printf("%i", value);
        
        printf("Outputting variable voltage from RB7");
        for(uint8_t count=0; count<=30; count++)
        {
            DAC1_SetOutput(count);
            printf(".");
            __delay_ms(250);

        }
        printf("\r\n");
        
        
        adc_result_t internal = ADC_GetConversion(channel_Temperature);
        adc_result_t sensor = ADC_GetConversion(channel_TEMP);
        // http://microcontrollerslab.com/temperature-sensor-using-pic16f877a-microcontroller/
        
        // ignoring the information, using the POT as a reference, the values
        // appear to go from zero to 4095 - so use that as the range
        float temp = sensor / 4095.0 * 100.0;
        //float temp = value - 0.5f;
        //temp = temp / 0.01f;
        
        //NOTE: printf passing %3.2f doesn't complile - grr
        printf("POT: %d, INT: %d, SEN: %d, TEMP: %d\r\n", pot, internal, sensor, ((int)temp));
        
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
}
/**
 End of File
*/