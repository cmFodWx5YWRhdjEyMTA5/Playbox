/**
  @Generated Pin Manager Header File

  @Company:
    Microchip Technology Inc.

  @File Name:
    pin_manager.h

  @Summary:
    This is the Pin Manager file generated using MPLAB(c) Code Configurator

  @Description:
    This header file provides implementations for pin APIs for all pins selected in the GUI.
    Generation Information :
        Product Revision  :  MPLAB(c) Code Configurator - 4.26
        Device            :  PIC16F1784
        Version           :  1.01
    The generated drivers are tested against the following:
        Compiler          :  XC8 1.35
        MPLAB             :  MPLAB X 3.40

    Copyright (c) 2013 - 2015 released Microchip Technology Inc.  All rights reserved.

    Microchip licenses to you the right to use, modify, copy and distribute
    Software only when embedded on a Microchip microcontroller or digital signal
    controller that is integrated into your product or third party product
    (pursuant to the sublicense terms in the accompanying license agreement).

    You should refer to the license agreement accompanying this Software for
    additional information regarding your rights and obligations.

    SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
    EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF
    MERCHANTABILITY, TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
    IN NO EVENT SHALL MICROCHIP OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER
    CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR
    OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
    INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR
    CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF
    SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
    (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.

*/


#ifndef PIN_MANAGER_H
#define PIN_MANAGER_H

#define INPUT   1
#define OUTPUT  0

#define HIGH    1
#define LOW     0

#define ANALOG      1
#define DIGITAL     0

#define PULL_UP_ENABLED      1
#define PULL_UP_DISABLED     0

// get/set LED aliases
#define LED_TRIS               TRISAbits.TRISA5
#define LED_LAT                LATAbits.LATA5
#define LED_PORT               PORTAbits.RA5
#define LED_WPU                WPUAbits.WPUA5
#define LED_ANS                ANSELAbits.ANSA5
#define LED_SetHigh()            do { LATAbits.LATA5 = 1; } while(0)
#define LED_SetLow()             do { LATAbits.LATA5 = 0; } while(0)
#define LED_Toggle()             do { LATAbits.LATA5 = ~LATAbits.LATA5; } while(0)
#define LED_GetValue()           PORTAbits.RA5
#define LED_SetDigitalInput()    do { TRISAbits.TRISA5 = 1; } while(0)
#define LED_SetDigitalOutput()   do { TRISAbits.TRISA5 = 0; } while(0)
#define LED_SetPullup()      do { WPUAbits.WPUA5 = 1; } while(0)
#define LED_ResetPullup()    do { WPUAbits.WPUA5 = 0; } while(0)
#define LED_SetAnalogMode()  do { ANSELAbits.ANSA5 = 1; } while(0)
#define LED_SetDigitalMode() do { ANSELAbits.ANSA5 = 0; } while(0)

// get/set channel_TEMP aliases
#define channel_TEMP_TRIS               TRISBbits.TRISB0
#define channel_TEMP_LAT                LATBbits.LATB0
#define channel_TEMP_PORT               PORTBbits.RB0
#define channel_TEMP_WPU                WPUBbits.WPUB0
#define channel_TEMP_ANS                ANSELBbits.ANSB0
#define channel_TEMP_SetHigh()            do { LATBbits.LATB0 = 1; } while(0)
#define channel_TEMP_SetLow()             do { LATBbits.LATB0 = 0; } while(0)
#define channel_TEMP_Toggle()             do { LATBbits.LATB0 = ~LATBbits.LATB0; } while(0)
#define channel_TEMP_GetValue()           PORTBbits.RB0
#define channel_TEMP_SetDigitalInput()    do { TRISBbits.TRISB0 = 1; } while(0)
#define channel_TEMP_SetDigitalOutput()   do { TRISBbits.TRISB0 = 0; } while(0)
#define channel_TEMP_SetPullup()      do { WPUBbits.WPUB0 = 1; } while(0)
#define channel_TEMP_ResetPullup()    do { WPUBbits.WPUB0 = 0; } while(0)
#define channel_TEMP_SetAnalogMode()  do { ANSELBbits.ANSB0 = 1; } while(0)
#define channel_TEMP_SetDigitalMode() do { ANSELBbits.ANSB0 = 0; } while(0)

// get/set RC6 procedures
#define RC6_SetHigh()    do { LATCbits.LATC6 = 1; } while(0)
#define RC6_SetLow()   do { LATCbits.LATC6 = 0; } while(0)
#define RC6_Toggle()   do { LATCbits.LATC6 = ~LATCbits.LATC6; } while(0)
#define RC6_GetValue()         PORTCbits.RC6
#define RC6_SetDigitalInput()   do { TRISCbits.TRISC6 = 1; } while(0)
#define RC6_SetDigitalOutput()  do { TRISCbits.TRISC6 = 0; } while(0)
#define RC6_SetPullup()     do { WPUCbits.WPUC6 = 1; } while(0)
#define RC6_ResetPullup()   do { WPUCbits.WPUC6 = 0; } while(0)

// get/set RC7 procedures
#define RC7_SetHigh()    do { LATCbits.LATC7 = 1; } while(0)
#define RC7_SetLow()   do { LATCbits.LATC7 = 0; } while(0)
#define RC7_Toggle()   do { LATCbits.LATC7 = ~LATCbits.LATC7; } while(0)
#define RC7_GetValue()         PORTCbits.RC7
#define RC7_SetDigitalInput()   do { TRISCbits.TRISC7 = 1; } while(0)
#define RC7_SetDigitalOutput()  do { TRISCbits.TRISC7 = 0; } while(0)
#define RC7_SetPullup()     do { WPUCbits.WPUC7 = 1; } while(0)
#define RC7_ResetPullup()   do { WPUCbits.WPUC7 = 0; } while(0)

/**
   @Param
    none
   @Returns
    none
   @Description
    GPIO and peripheral I/O initialization
   @Example
    PIN_MANAGER_Initialize();
 */
void PIN_MANAGER_Initialize (void);

/**
 * @Param
    none
 * @Returns
    none
 * @Description
    Interrupt on Change Handling routine
 * @Example
    PIN_MANAGER_IOC();
 */
void PIN_MANAGER_IOC(void);



#endif // PIN_MANAGER_H
/**
 End of File
*/