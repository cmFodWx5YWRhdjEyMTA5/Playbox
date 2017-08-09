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
        Device            :  PIC16F1787
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

// get/set channel_POT aliases
#define channel_POT_TRIS               TRISAbits.TRISA0
#define channel_POT_LAT                LATAbits.LATA0
#define channel_POT_PORT               PORTAbits.RA0
#define channel_POT_WPU                WPUAbits.WPUA0
#define channel_POT_ANS                ANSELAbits.ANSA0
#define channel_POT_SetHigh()            do { LATAbits.LATA0 = 1; } while(0)
#define channel_POT_SetLow()             do { LATAbits.LATA0 = 0; } while(0)
#define channel_POT_Toggle()             do { LATAbits.LATA0 = ~LATAbits.LATA0; } while(0)
#define channel_POT_GetValue()           PORTAbits.RA0
#define channel_POT_SetDigitalInput()    do { TRISAbits.TRISA0 = 1; } while(0)
#define channel_POT_SetDigitalOutput()   do { TRISAbits.TRISA0 = 0; } while(0)
#define channel_POT_SetPullup()      do { WPUAbits.WPUA0 = 1; } while(0)
#define channel_POT_ResetPullup()    do { WPUAbits.WPUA0 = 0; } while(0)
#define channel_POT_SetAnalogMode()  do { ANSELAbits.ANSA0 = 1; } while(0)
#define channel_POT_SetDigitalMode() do { ANSELAbits.ANSA0 = 0; } while(0)

// get/set IO_BTN aliases
#define IO_BTN_TRIS               TRISAbits.TRISA4
#define IO_BTN_LAT                LATAbits.LATA4
#define IO_BTN_PORT               PORTAbits.RA4
#define IO_BTN_WPU                WPUAbits.WPUA4
#define IO_BTN_ANS                ANSELAbits.ANSA4
#define IO_BTN_SetHigh()            do { LATAbits.LATA4 = 1; } while(0)
#define IO_BTN_SetLow()             do { LATAbits.LATA4 = 0; } while(0)
#define IO_BTN_Toggle()             do { LATAbits.LATA4 = ~LATAbits.LATA4; } while(0)
#define IO_BTN_GetValue()           PORTAbits.RA4
#define IO_BTN_SetDigitalInput()    do { TRISAbits.TRISA4 = 1; } while(0)
#define IO_BTN_SetDigitalOutput()   do { TRISAbits.TRISA4 = 0; } while(0)
#define IO_BTN_SetPullup()      do { WPUAbits.WPUA4 = 1; } while(0)
#define IO_BTN_ResetPullup()    do { WPUAbits.WPUA4 = 0; } while(0)
#define IO_BTN_SetAnalogMode()  do { ANSELAbits.ANSA4 = 1; } while(0)
#define IO_BTN_SetDigitalMode() do { ANSELAbits.ANSA4 = 0; } while(0)

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

// get/set IO_SLV aliases
#define IO_SLV_TRIS               TRISAbits.TRISA7
#define IO_SLV_LAT                LATAbits.LATA7
#define IO_SLV_PORT               PORTAbits.RA7
#define IO_SLV_WPU                WPUAbits.WPUA7
#define IO_SLV_ANS                ANSELAbits.ANSA7
#define IO_SLV_SetHigh()            do { LATAbits.LATA7 = 1; } while(0)
#define IO_SLV_SetLow()             do { LATAbits.LATA7 = 0; } while(0)
#define IO_SLV_Toggle()             do { LATAbits.LATA7 = ~LATAbits.LATA7; } while(0)
#define IO_SLV_GetValue()           PORTAbits.RA7
#define IO_SLV_SetDigitalInput()    do { TRISAbits.TRISA7 = 1; } while(0)
#define IO_SLV_SetDigitalOutput()   do { TRISAbits.TRISA7 = 0; } while(0)
#define IO_SLV_SetPullup()      do { WPUAbits.WPUA7 = 1; } while(0)
#define IO_SLV_ResetPullup()    do { WPUAbits.WPUA7 = 0; } while(0)
#define IO_SLV_SetAnalogMode()  do { ANSELAbits.ANSA7 = 1; } while(0)
#define IO_SLV_SetDigitalMode() do { ANSELAbits.ANSA7 = 0; } while(0)

// get/set channel_HPT aliases
#define channel_HPT_TRIS               TRISBbits.TRISB0
#define channel_HPT_LAT                LATBbits.LATB0
#define channel_HPT_PORT               PORTBbits.RB0
#define channel_HPT_WPU                WPUBbits.WPUB0
#define channel_HPT_ANS                ANSELBbits.ANSB0
#define channel_HPT_SetHigh()            do { LATBbits.LATB0 = 1; } while(0)
#define channel_HPT_SetLow()             do { LATBbits.LATB0 = 0; } while(0)
#define channel_HPT_Toggle()             do { LATBbits.LATB0 = ~LATBbits.LATB0; } while(0)
#define channel_HPT_GetValue()           PORTBbits.RB0
#define channel_HPT_SetDigitalInput()    do { TRISBbits.TRISB0 = 1; } while(0)
#define channel_HPT_SetDigitalOutput()   do { TRISBbits.TRISB0 = 0; } while(0)
#define channel_HPT_SetPullup()      do { WPUBbits.WPUB0 = 1; } while(0)
#define channel_HPT_ResetPullup()    do { WPUBbits.WPUB0 = 0; } while(0)
#define channel_HPT_SetAnalogMode()  do { ANSELBbits.ANSB0 = 1; } while(0)
#define channel_HPT_SetDigitalMode() do { ANSELBbits.ANSB0 = 0; } while(0)

// get/set channel_WT aliases
#define channel_WT_TRIS               TRISBbits.TRISB1
#define channel_WT_LAT                LATBbits.LATB1
#define channel_WT_PORT               PORTBbits.RB1
#define channel_WT_WPU                WPUBbits.WPUB1
#define channel_WT_ANS                ANSELBbits.ANSB1
#define channel_WT_SetHigh()            do { LATBbits.LATB1 = 1; } while(0)
#define channel_WT_SetLow()             do { LATBbits.LATB1 = 0; } while(0)
#define channel_WT_Toggle()             do { LATBbits.LATB1 = ~LATBbits.LATB1; } while(0)
#define channel_WT_GetValue()           PORTBbits.RB1
#define channel_WT_SetDigitalInput()    do { TRISBbits.TRISB1 = 1; } while(0)
#define channel_WT_SetDigitalOutput()   do { TRISBbits.TRISB1 = 0; } while(0)
#define channel_WT_SetPullup()      do { WPUBbits.WPUB1 = 1; } while(0)
#define channel_WT_ResetPullup()    do { WPUBbits.WPUB1 = 0; } while(0)
#define channel_WT_SetAnalogMode()  do { ANSELBbits.ANSB1 = 1; } while(0)
#define channel_WT_SetDigitalMode() do { ANSELBbits.ANSB1 = 0; } while(0)

// get/set IO_TM1 aliases
#define IO_TM1_TRIS               TRISBbits.TRISB3
#define IO_TM1_LAT                LATBbits.LATB3
#define IO_TM1_PORT               PORTBbits.RB3
#define IO_TM1_WPU                WPUBbits.WPUB3
#define IO_TM1_ANS                ANSELBbits.ANSB3
#define IO_TM1_SetHigh()            do { LATBbits.LATB3 = 1; } while(0)
#define IO_TM1_SetLow()             do { LATBbits.LATB3 = 0; } while(0)
#define IO_TM1_Toggle()             do { LATBbits.LATB3 = ~LATBbits.LATB3; } while(0)
#define IO_TM1_GetValue()           PORTBbits.RB3
#define IO_TM1_SetDigitalInput()    do { TRISBbits.TRISB3 = 1; } while(0)
#define IO_TM1_SetDigitalOutput()   do { TRISBbits.TRISB3 = 0; } while(0)
#define IO_TM1_SetPullup()      do { WPUBbits.WPUB3 = 1; } while(0)
#define IO_TM1_ResetPullup()    do { WPUBbits.WPUB3 = 0; } while(0)
#define IO_TM1_SetAnalogMode()  do { ANSELBbits.ANSB3 = 1; } while(0)
#define IO_TM1_SetDigitalMode() do { ANSELBbits.ANSB3 = 0; } while(0)

// get/set IO_TM2 aliases
#define IO_TM2_TRIS               TRISBbits.TRISB4
#define IO_TM2_LAT                LATBbits.LATB4
#define IO_TM2_PORT               PORTBbits.RB4
#define IO_TM2_WPU                WPUBbits.WPUB4
#define IO_TM2_ANS                ANSELBbits.ANSB4
#define IO_TM2_SetHigh()            do { LATBbits.LATB4 = 1; } while(0)
#define IO_TM2_SetLow()             do { LATBbits.LATB4 = 0; } while(0)
#define IO_TM2_Toggle()             do { LATBbits.LATB4 = ~LATBbits.LATB4; } while(0)
#define IO_TM2_GetValue()           PORTBbits.RB4
#define IO_TM2_SetDigitalInput()    do { TRISBbits.TRISB4 = 1; } while(0)
#define IO_TM2_SetDigitalOutput()   do { TRISBbits.TRISB4 = 0; } while(0)
#define IO_TM2_SetPullup()      do { WPUBbits.WPUB4 = 1; } while(0)
#define IO_TM2_ResetPullup()    do { WPUBbits.WPUB4 = 0; } while(0)
#define IO_TM2_SetAnalogMode()  do { ANSELBbits.ANSB4 = 1; } while(0)
#define IO_TM2_SetDigitalMode() do { ANSELBbits.ANSB4 = 0; } while(0)

// get/set IO_TM3 aliases
#define IO_TM3_TRIS               TRISBbits.TRISB5
#define IO_TM3_LAT                LATBbits.LATB5
#define IO_TM3_PORT               PORTBbits.RB5
#define IO_TM3_WPU                WPUBbits.WPUB5
#define IO_TM3_ANS                ANSELBbits.ANSB5
#define IO_TM3_SetHigh()            do { LATBbits.LATB5 = 1; } while(0)
#define IO_TM3_SetLow()             do { LATBbits.LATB5 = 0; } while(0)
#define IO_TM3_Toggle()             do { LATBbits.LATB5 = ~LATBbits.LATB5; } while(0)
#define IO_TM3_GetValue()           PORTBbits.RB5
#define IO_TM3_SetDigitalInput()    do { TRISBbits.TRISB5 = 1; } while(0)
#define IO_TM3_SetDigitalOutput()   do { TRISBbits.TRISB5 = 0; } while(0)
#define IO_TM3_SetPullup()      do { WPUBbits.WPUB5 = 1; } while(0)
#define IO_TM3_ResetPullup()    do { WPUBbits.WPUB5 = 0; } while(0)
#define IO_TM3_SetAnalogMode()  do { ANSELBbits.ANSB5 = 1; } while(0)
#define IO_TM3_SetDigitalMode() do { ANSELBbits.ANSB5 = 0; } while(0)

// get/set IO_TM4 aliases
#define IO_TM4_TRIS               TRISBbits.TRISB6
#define IO_TM4_LAT                LATBbits.LATB6
#define IO_TM4_PORT               PORTBbits.RB6
#define IO_TM4_WPU                WPUBbits.WPUB6
#define IO_TM4_ANS                ANSELBbits.ANSB6
#define IO_TM4_SetHigh()            do { LATBbits.LATB6 = 1; } while(0)
#define IO_TM4_SetLow()             do { LATBbits.LATB6 = 0; } while(0)
#define IO_TM4_Toggle()             do { LATBbits.LATB6 = ~LATBbits.LATB6; } while(0)
#define IO_TM4_GetValue()           PORTBbits.RB6
#define IO_TM4_SetDigitalInput()    do { TRISBbits.TRISB6 = 1; } while(0)
#define IO_TM4_SetDigitalOutput()   do { TRISBbits.TRISB6 = 0; } while(0)
#define IO_TM4_SetPullup()      do { WPUBbits.WPUB6 = 1; } while(0)
#define IO_TM4_ResetPullup()    do { WPUBbits.WPUB6 = 0; } while(0)
#define IO_TM4_SetAnalogMode()  do { ANSELBbits.ANSB6 = 1; } while(0)
#define IO_TM4_SetDigitalMode() do { ANSELBbits.ANSB6 = 0; } while(0)

// get/set RB7 procedures
#define RB7_SetHigh()    do { LATBbits.LATB7 = 1; } while(0)
#define RB7_SetLow()   do { LATBbits.LATB7 = 0; } while(0)
#define RB7_Toggle()   do { LATBbits.LATB7 = ~LATBbits.LATB7; } while(0)
#define RB7_GetValue()         PORTBbits.RB7
#define RB7_SetDigitalInput()   do { TRISBbits.TRISB7 = 1; } while(0)
#define RB7_SetDigitalOutput()  do { TRISBbits.TRISB7 = 0; } while(0)
#define RB7_SetPullup()     do { WPUBbits.WPUB7 = 1; } while(0)
#define RB7_ResetPullup()   do { WPUBbits.WPUB7 = 0; } while(0)

// get/set IO_TM5 aliases
#define IO_TM5_TRIS               TRISCbits.TRISC0
#define IO_TM5_LAT                LATCbits.LATC0
#define IO_TM5_PORT               PORTCbits.RC0
#define IO_TM5_WPU                WPUCbits.WPUC0
#define IO_TM5_SetHigh()            do { LATCbits.LATC0 = 1; } while(0)
#define IO_TM5_SetLow()             do { LATCbits.LATC0 = 0; } while(0)
#define IO_TM5_Toggle()             do { LATCbits.LATC0 = ~LATCbits.LATC0; } while(0)
#define IO_TM5_GetValue()           PORTCbits.RC0
#define IO_TM5_SetDigitalInput()    do { TRISCbits.TRISC0 = 1; } while(0)
#define IO_TM5_SetDigitalOutput()   do { TRISCbits.TRISC0 = 0; } while(0)
#define IO_TM5_SetPullup()      do { WPUCbits.WPUC0 = 1; } while(0)
#define IO_TM5_ResetPullup()    do { WPUCbits.WPUC0 = 0; } while(0)

// get/set RC1 procedures
#define RC1_SetHigh()    do { LATCbits.LATC1 = 1; } while(0)
#define RC1_SetLow()   do { LATCbits.LATC1 = 0; } while(0)
#define RC1_Toggle()   do { LATCbits.LATC1 = ~LATCbits.LATC1; } while(0)
#define RC1_GetValue()         PORTCbits.RC1
#define RC1_SetDigitalInput()   do { TRISCbits.TRISC1 = 1; } while(0)
#define RC1_SetDigitalOutput()  do { TRISCbits.TRISC1 = 0; } while(0)
#define RC1_SetPullup()     do { WPUCbits.WPUC1 = 1; } while(0)
#define RC1_ResetPullup()   do { WPUCbits.WPUC1 = 0; } while(0)

// get/set RC2 procedures
#define RC2_SetHigh()    do { LATCbits.LATC2 = 1; } while(0)
#define RC2_SetLow()   do { LATCbits.LATC2 = 0; } while(0)
#define RC2_Toggle()   do { LATCbits.LATC2 = ~LATCbits.LATC2; } while(0)
#define RC2_GetValue()         PORTCbits.RC2
#define RC2_SetDigitalInput()   do { TRISCbits.TRISC2 = 1; } while(0)
#define RC2_SetDigitalOutput()  do { TRISCbits.TRISC2 = 0; } while(0)
#define RC2_SetPullup()     do { WPUCbits.WPUC2 = 1; } while(0)
#define RC2_ResetPullup()   do { WPUCbits.WPUC2 = 0; } while(0)

// get/set RC3 procedures
#define RC3_SetHigh()    do { LATCbits.LATC3 = 1; } while(0)
#define RC3_SetLow()   do { LATCbits.LATC3 = 0; } while(0)
#define RC3_Toggle()   do { LATCbits.LATC3 = ~LATCbits.LATC3; } while(0)
#define RC3_GetValue()         PORTCbits.RC3
#define RC3_SetDigitalInput()   do { TRISCbits.TRISC3 = 1; } while(0)
#define RC3_SetDigitalOutput()  do { TRISCbits.TRISC3 = 0; } while(0)
#define RC3_SetPullup()     do { WPUCbits.WPUC3 = 1; } while(0)
#define RC3_ResetPullup()   do { WPUCbits.WPUC3 = 0; } while(0)

// get/set RC4 procedures
#define RC4_SetHigh()    do { LATCbits.LATC4 = 1; } while(0)
#define RC4_SetLow()   do { LATCbits.LATC4 = 0; } while(0)
#define RC4_Toggle()   do { LATCbits.LATC4 = ~LATCbits.LATC4; } while(0)
#define RC4_GetValue()         PORTCbits.RC4
#define RC4_SetDigitalInput()   do { TRISCbits.TRISC4 = 1; } while(0)
#define RC4_SetDigitalOutput()  do { TRISCbits.TRISC4 = 0; } while(0)
#define RC4_SetPullup()     do { WPUCbits.WPUC4 = 1; } while(0)
#define RC4_ResetPullup()   do { WPUCbits.WPUC4 = 0; } while(0)

// get/set IO_TM6 aliases
#define IO_TM6_TRIS               TRISCbits.TRISC5
#define IO_TM6_LAT                LATCbits.LATC5
#define IO_TM6_PORT               PORTCbits.RC5
#define IO_TM6_WPU                WPUCbits.WPUC5
#define IO_TM6_SetHigh()            do { LATCbits.LATC5 = 1; } while(0)
#define IO_TM6_SetLow()             do { LATCbits.LATC5 = 0; } while(0)
#define IO_TM6_Toggle()             do { LATCbits.LATC5 = ~LATCbits.LATC5; } while(0)
#define IO_TM6_GetValue()           PORTCbits.RC5
#define IO_TM6_SetDigitalInput()    do { TRISCbits.TRISC5 = 1; } while(0)
#define IO_TM6_SetDigitalOutput()   do { TRISCbits.TRISC5 = 0; } while(0)
#define IO_TM6_SetPullup()      do { WPUCbits.WPUC5 = 1; } while(0)
#define IO_TM6_ResetPullup()    do { WPUCbits.WPUC5 = 0; } while(0)

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

// get/set IO_TM7 aliases
#define IO_TM7_TRIS               TRISDbits.TRISD0
#define IO_TM7_LAT                LATDbits.LATD0
#define IO_TM7_PORT               PORTDbits.RD0
#define IO_TM7_WPU                WPUDbits.WPUD0
#define IO_TM7_ANS                ANSELDbits.ANSD0
#define IO_TM7_SetHigh()            do { LATDbits.LATD0 = 1; } while(0)
#define IO_TM7_SetLow()             do { LATDbits.LATD0 = 0; } while(0)
#define IO_TM7_Toggle()             do { LATDbits.LATD0 = ~LATDbits.LATD0; } while(0)
#define IO_TM7_GetValue()           PORTDbits.RD0
#define IO_TM7_SetDigitalInput()    do { TRISDbits.TRISD0 = 1; } while(0)
#define IO_TM7_SetDigitalOutput()   do { TRISDbits.TRISD0 = 0; } while(0)
#define IO_TM7_SetPullup()      do { WPUDbits.WPUD0 = 1; } while(0)
#define IO_TM7_ResetPullup()    do { WPUDbits.WPUD0 = 0; } while(0)
#define IO_TM7_SetAnalogMode()  do { ANSELDbits.ANSD0 = 1; } while(0)
#define IO_TM7_SetDigitalMode() do { ANSELDbits.ANSD0 = 0; } while(0)

// get/set IO_TM8 aliases
#define IO_TM8_TRIS               TRISDbits.TRISD1
#define IO_TM8_LAT                LATDbits.LATD1
#define IO_TM8_PORT               PORTDbits.RD1
#define IO_TM8_WPU                WPUDbits.WPUD1
#define IO_TM8_ANS                ANSELDbits.ANSD1
#define IO_TM8_SetHigh()            do { LATDbits.LATD1 = 1; } while(0)
#define IO_TM8_SetLow()             do { LATDbits.LATD1 = 0; } while(0)
#define IO_TM8_Toggle()             do { LATDbits.LATD1 = ~LATDbits.LATD1; } while(0)
#define IO_TM8_GetValue()           PORTDbits.RD1
#define IO_TM8_SetDigitalInput()    do { TRISDbits.TRISD1 = 1; } while(0)
#define IO_TM8_SetDigitalOutput()   do { TRISDbits.TRISD1 = 0; } while(0)
#define IO_TM8_SetPullup()      do { WPUDbits.WPUD1 = 1; } while(0)
#define IO_TM8_ResetPullup()    do { WPUDbits.WPUD1 = 0; } while(0)
#define IO_TM8_SetAnalogMode()  do { ANSELDbits.ANSD1 = 1; } while(0)
#define IO_TM8_SetDigitalMode() do { ANSELDbits.ANSD1 = 0; } while(0)

// get/set IO_TM9 aliases
#define IO_TM9_TRIS               TRISDbits.TRISD2
#define IO_TM9_LAT                LATDbits.LATD2
#define IO_TM9_PORT               PORTDbits.RD2
#define IO_TM9_WPU                WPUDbits.WPUD2
#define IO_TM9_ANS                ANSELDbits.ANSD2
#define IO_TM9_SetHigh()            do { LATDbits.LATD2 = 1; } while(0)
#define IO_TM9_SetLow()             do { LATDbits.LATD2 = 0; } while(0)
#define IO_TM9_Toggle()             do { LATDbits.LATD2 = ~LATDbits.LATD2; } while(0)
#define IO_TM9_GetValue()           PORTDbits.RD2
#define IO_TM9_SetDigitalInput()    do { TRISDbits.TRISD2 = 1; } while(0)
#define IO_TM9_SetDigitalOutput()   do { TRISDbits.TRISD2 = 0; } while(0)
#define IO_TM9_SetPullup()      do { WPUDbits.WPUD2 = 1; } while(0)
#define IO_TM9_ResetPullup()    do { WPUDbits.WPUD2 = 0; } while(0)
#define IO_TM9_SetAnalogMode()  do { ANSELDbits.ANSD2 = 1; } while(0)
#define IO_TM9_SetDigitalMode() do { ANSELDbits.ANSD2 = 0; } while(0)

// get/set IO_TM10 aliases
#define IO_TM10_TRIS               TRISDbits.TRISD3
#define IO_TM10_LAT                LATDbits.LATD3
#define IO_TM10_PORT               PORTDbits.RD3
#define IO_TM10_WPU                WPUDbits.WPUD3
#define IO_TM10_SetHigh()            do { LATDbits.LATD3 = 1; } while(0)
#define IO_TM10_SetLow()             do { LATDbits.LATD3 = 0; } while(0)
#define IO_TM10_Toggle()             do { LATDbits.LATD3 = ~LATDbits.LATD3; } while(0)
#define IO_TM10_GetValue()           PORTDbits.RD3
#define IO_TM10_SetDigitalInput()    do { TRISDbits.TRISD3 = 1; } while(0)
#define IO_TM10_SetDigitalOutput()   do { TRISDbits.TRISD3 = 0; } while(0)
#define IO_TM10_SetPullup()      do { WPUDbits.WPUD3 = 1; } while(0)
#define IO_TM10_ResetPullup()    do { WPUDbits.WPUD3 = 0; } while(0)

// get/set IO_TM11 aliases
#define IO_TM11_TRIS               TRISDbits.TRISD4
#define IO_TM11_LAT                LATDbits.LATD4
#define IO_TM11_PORT               PORTDbits.RD4
#define IO_TM11_WPU                WPUDbits.WPUD4
#define IO_TM11_SetHigh()            do { LATDbits.LATD4 = 1; } while(0)
#define IO_TM11_SetLow()             do { LATDbits.LATD4 = 0; } while(0)
#define IO_TM11_Toggle()             do { LATDbits.LATD4 = ~LATDbits.LATD4; } while(0)
#define IO_TM11_GetValue()           PORTDbits.RD4
#define IO_TM11_SetDigitalInput()    do { TRISDbits.TRISD4 = 1; } while(0)
#define IO_TM11_SetDigitalOutput()   do { TRISDbits.TRISD4 = 0; } while(0)
#define IO_TM11_SetPullup()      do { WPUDbits.WPUD4 = 1; } while(0)
#define IO_TM11_ResetPullup()    do { WPUDbits.WPUD4 = 0; } while(0)

// get/set IO_TM12 aliases
#define IO_TM12_TRIS               TRISDbits.TRISD5
#define IO_TM12_LAT                LATDbits.LATD5
#define IO_TM12_PORT               PORTDbits.RD5
#define IO_TM12_WPU                WPUDbits.WPUD5
#define IO_TM12_SetHigh()            do { LATDbits.LATD5 = 1; } while(0)
#define IO_TM12_SetLow()             do { LATDbits.LATD5 = 0; } while(0)
#define IO_TM12_Toggle()             do { LATDbits.LATD5 = ~LATDbits.LATD5; } while(0)
#define IO_TM12_GetValue()           PORTDbits.RD5
#define IO_TM12_SetDigitalInput()    do { TRISDbits.TRISD5 = 1; } while(0)
#define IO_TM12_SetDigitalOutput()   do { TRISDbits.TRISD5 = 0; } while(0)
#define IO_TM12_SetPullup()      do { WPUDbits.WPUD5 = 1; } while(0)
#define IO_TM12_ResetPullup()    do { WPUDbits.WPUD5 = 0; } while(0)

// get/set IO_TMAM aliases
#define IO_TMAM_TRIS               TRISDbits.TRISD6
#define IO_TMAM_LAT                LATDbits.LATD6
#define IO_TMAM_PORT               PORTDbits.RD6
#define IO_TMAM_WPU                WPUDbits.WPUD6
#define IO_TMAM_SetHigh()            do { LATDbits.LATD6 = 1; } while(0)
#define IO_TMAM_SetLow()             do { LATDbits.LATD6 = 0; } while(0)
#define IO_TMAM_Toggle()             do { LATDbits.LATD6 = ~LATDbits.LATD6; } while(0)
#define IO_TMAM_GetValue()           PORTDbits.RD6
#define IO_TMAM_SetDigitalInput()    do { TRISDbits.TRISD6 = 1; } while(0)
#define IO_TMAM_SetDigitalOutput()   do { TRISDbits.TRISD6 = 0; } while(0)
#define IO_TMAM_SetPullup()      do { WPUDbits.WPUD6 = 1; } while(0)
#define IO_TMAM_ResetPullup()    do { WPUDbits.WPUD6 = 0; } while(0)

// get/set IO_TMPM aliases
#define IO_TMPM_TRIS               TRISDbits.TRISD7
#define IO_TMPM_LAT                LATDbits.LATD7
#define IO_TMPM_PORT               PORTDbits.RD7
#define IO_TMPM_WPU                WPUDbits.WPUD7
#define IO_TMPM_SetHigh()            do { LATDbits.LATD7 = 1; } while(0)
#define IO_TMPM_SetLow()             do { LATDbits.LATD7 = 0; } while(0)
#define IO_TMPM_Toggle()             do { LATDbits.LATD7 = ~LATDbits.LATD7; } while(0)
#define IO_TMPM_GetValue()           PORTDbits.RD7
#define IO_TMPM_SetDigitalInput()    do { TRISDbits.TRISD7 = 1; } while(0)
#define IO_TMPM_SetDigitalOutput()   do { TRISDbits.TRISD7 = 0; } while(0)
#define IO_TMPM_SetPullup()      do { WPUDbits.WPUD7 = 1; } while(0)
#define IO_TMPM_ResetPullup()    do { WPUDbits.WPUD7 = 0; } while(0)

// get/set RE0 procedures
#define RE0_SetHigh()    do { LATEbits.LATE0 = 1; } while(0)
#define RE0_SetLow()   do { LATEbits.LATE0 = 0; } while(0)
#define RE0_Toggle()   do { LATEbits.LATE0 = ~LATEbits.LATE0; } while(0)
#define RE0_GetValue()         PORTEbits.RE0
#define RE0_SetDigitalInput()   do { TRISEbits.TRISE0 = 1; } while(0)
#define RE0_SetDigitalOutput()  do { TRISEbits.TRISE0 = 0; } while(0)
#define RE0_SetPullup()     do { WPUEbits.WPUE0 = 1; } while(0)
#define RE0_ResetPullup()   do { WPUEbits.WPUE0 = 0; } while(0)
#define RE0_SetAnalogMode() do { ANSELEbits.ANSE0 = 1; } while(0)
#define RE0_SetDigitalMode()do { ANSELEbits.ANSE0 = 0; } while(0)

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