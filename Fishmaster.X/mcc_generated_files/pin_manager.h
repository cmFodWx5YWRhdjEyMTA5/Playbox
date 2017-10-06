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

// get/set IO_PWR aliases
#define IO_PWR_TRIS               TRISAbits.TRISA0
#define IO_PWR_LAT                LATAbits.LATA0
#define IO_PWR_PORT               PORTAbits.RA0
#define IO_PWR_WPU                WPUAbits.WPUA0
#define IO_PWR_ANS                ANSELAbits.ANSA0
#define IO_PWR_SetHigh()            do { LATAbits.LATA0 = 1; } while(0)
#define IO_PWR_SetLow()             do { LATAbits.LATA0 = 0; } while(0)
#define IO_PWR_Toggle()             do { LATAbits.LATA0 = ~LATAbits.LATA0; } while(0)
#define IO_PWR_GetValue()           PORTAbits.RA0
#define IO_PWR_SetDigitalInput()    do { TRISAbits.TRISA0 = 1; } while(0)
#define IO_PWR_SetDigitalOutput()   do { TRISAbits.TRISA0 = 0; } while(0)
#define IO_PWR_SetPullup()      do { WPUAbits.WPUA0 = 1; } while(0)
#define IO_PWR_ResetPullup()    do { WPUAbits.WPUA0 = 0; } while(0)
#define IO_PWR_SetAnalogMode()  do { ANSELAbits.ANSA0 = 1; } while(0)
#define IO_PWR_SetDigitalMode() do { ANSELAbits.ANSA0 = 0; } while(0)

// get/set channel_INPT aliases
#define channel_INPT_TRIS               TRISAbits.TRISA1
#define channel_INPT_LAT                LATAbits.LATA1
#define channel_INPT_PORT               PORTAbits.RA1
#define channel_INPT_WPU                WPUAbits.WPUA1
#define channel_INPT_ANS                ANSELAbits.ANSA1
#define channel_INPT_SetHigh()            do { LATAbits.LATA1 = 1; } while(0)
#define channel_INPT_SetLow()             do { LATAbits.LATA1 = 0; } while(0)
#define channel_INPT_Toggle()             do { LATAbits.LATA1 = ~LATAbits.LATA1; } while(0)
#define channel_INPT_GetValue()           PORTAbits.RA1
#define channel_INPT_SetDigitalInput()    do { TRISAbits.TRISA1 = 1; } while(0)
#define channel_INPT_SetDigitalOutput()   do { TRISAbits.TRISA1 = 0; } while(0)
#define channel_INPT_SetPullup()      do { WPUAbits.WPUA1 = 1; } while(0)
#define channel_INPT_ResetPullup()    do { WPUAbits.WPUA1 = 0; } while(0)
#define channel_INPT_SetAnalogMode()  do { ANSELAbits.ANSA1 = 1; } while(0)
#define channel_INPT_SetDigitalMode() do { ANSELAbits.ANSA1 = 0; } while(0)

// get/set RA2 procedures
#define RA2_SetHigh()    do { LATAbits.LATA2 = 1; } while(0)
#define RA2_SetLow()   do { LATAbits.LATA2 = 0; } while(0)
#define RA2_Toggle()   do { LATAbits.LATA2 = ~LATAbits.LATA2; } while(0)
#define RA2_GetValue()         PORTAbits.RA2
#define RA2_SetDigitalInput()   do { TRISAbits.TRISA2 = 1; } while(0)
#define RA2_SetDigitalOutput()  do { TRISAbits.TRISA2 = 0; } while(0)
#define RA2_SetPullup()     do { WPUAbits.WPUA2 = 1; } while(0)
#define RA2_ResetPullup()   do { WPUAbits.WPUA2 = 0; } while(0)
#define RA2_SetAnalogMode() do { ANSELAbits.ANSA2 = 1; } while(0)
#define RA2_SetDigitalMode()do { ANSELAbits.ANSA2 = 0; } while(0)

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

// get/set IO_BTN aliases
#define IO_BTN_TRIS               TRISBbits.TRISB2
#define IO_BTN_LAT                LATBbits.LATB2
#define IO_BTN_PORT               PORTBbits.RB2
#define IO_BTN_WPU                WPUBbits.WPUB2
#define IO_BTN_ANS                ANSELBbits.ANSB2
#define IO_BTN_SetHigh()            do { LATBbits.LATB2 = 1; } while(0)
#define IO_BTN_SetLow()             do { LATBbits.LATB2 = 0; } while(0)
#define IO_BTN_Toggle()             do { LATBbits.LATB2 = ~LATBbits.LATB2; } while(0)
#define IO_BTN_GetValue()           PORTBbits.RB2
#define IO_BTN_SetDigitalInput()    do { TRISBbits.TRISB2 = 1; } while(0)
#define IO_BTN_SetDigitalOutput()   do { TRISBbits.TRISB2 = 0; } while(0)
#define IO_BTN_SetPullup()      do { WPUBbits.WPUB2 = 1; } while(0)
#define IO_BTN_ResetPullup()    do { WPUBbits.WPUB2 = 0; } while(0)
#define IO_BTN_SetAnalogMode()  do { ANSELBbits.ANSB2 = 1; } while(0)
#define IO_BTN_SetDigitalMode() do { ANSELBbits.ANSB2 = 0; } while(0)

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

// get/set LED_COM aliases
#define LED_COM_TRIS               TRISDbits.TRISD0
#define LED_COM_LAT                LATDbits.LATD0
#define LED_COM_PORT               PORTDbits.RD0
#define LED_COM_WPU                WPUDbits.WPUD0
#define LED_COM_ANS                ANSELDbits.ANSD0
#define LED_COM_SetHigh()            do { LATDbits.LATD0 = 1; } while(0)
#define LED_COM_SetLow()             do { LATDbits.LATD0 = 0; } while(0)
#define LED_COM_Toggle()             do { LATDbits.LATD0 = ~LATDbits.LATD0; } while(0)
#define LED_COM_GetValue()           PORTDbits.RD0
#define LED_COM_SetDigitalInput()    do { TRISDbits.TRISD0 = 1; } while(0)
#define LED_COM_SetDigitalOutput()   do { TRISDbits.TRISD0 = 0; } while(0)
#define LED_COM_SetPullup()      do { WPUDbits.WPUD0 = 1; } while(0)
#define LED_COM_ResetPullup()    do { WPUDbits.WPUD0 = 0; } while(0)
#define LED_COM_SetAnalogMode()  do { ANSELDbits.ANSD0 = 1; } while(0)
#define LED_COM_SetDigitalMode() do { ANSELDbits.ANSD0 = 0; } while(0)

// get/set LED_11 aliases
#define LED_11_TRIS               TRISDbits.TRISD1
#define LED_11_LAT                LATDbits.LATD1
#define LED_11_PORT               PORTDbits.RD1
#define LED_11_WPU                WPUDbits.WPUD1
#define LED_11_ANS                ANSELDbits.ANSD1
#define LED_11_SetHigh()            do { LATDbits.LATD1 = 1; } while(0)
#define LED_11_SetLow()             do { LATDbits.LATD1 = 0; } while(0)
#define LED_11_Toggle()             do { LATDbits.LATD1 = ~LATDbits.LATD1; } while(0)
#define LED_11_GetValue()           PORTDbits.RD1
#define LED_11_SetDigitalInput()    do { TRISDbits.TRISD1 = 1; } while(0)
#define LED_11_SetDigitalOutput()   do { TRISDbits.TRISD1 = 0; } while(0)
#define LED_11_SetPullup()      do { WPUDbits.WPUD1 = 1; } while(0)
#define LED_11_ResetPullup()    do { WPUDbits.WPUD1 = 0; } while(0)
#define LED_11_SetAnalogMode()  do { ANSELDbits.ANSD1 = 1; } while(0)
#define LED_11_SetDigitalMode() do { ANSELDbits.ANSD1 = 0; } while(0)

// get/set LED_9_10 aliases
#define LED_9_10_TRIS               TRISDbits.TRISD2
#define LED_9_10_LAT                LATDbits.LATD2
#define LED_9_10_PORT               PORTDbits.RD2
#define LED_9_10_WPU                WPUDbits.WPUD2
#define LED_9_10_ANS                ANSELDbits.ANSD2
#define LED_9_10_SetHigh()            do { LATDbits.LATD2 = 1; } while(0)
#define LED_9_10_SetLow()             do { LATDbits.LATD2 = 0; } while(0)
#define LED_9_10_Toggle()             do { LATDbits.LATD2 = ~LATDbits.LATD2; } while(0)
#define LED_9_10_GetValue()           PORTDbits.RD2
#define LED_9_10_SetDigitalInput()    do { TRISDbits.TRISD2 = 1; } while(0)
#define LED_9_10_SetDigitalOutput()   do { TRISDbits.TRISD2 = 0; } while(0)
#define LED_9_10_SetPullup()      do { WPUDbits.WPUD2 = 1; } while(0)
#define LED_9_10_ResetPullup()    do { WPUDbits.WPUD2 = 0; } while(0)
#define LED_9_10_SetAnalogMode()  do { ANSELDbits.ANSD2 = 1; } while(0)
#define LED_9_10_SetDigitalMode() do { ANSELDbits.ANSD2 = 0; } while(0)

// get/set LED_7_8 aliases
#define LED_7_8_TRIS               TRISDbits.TRISD3
#define LED_7_8_LAT                LATDbits.LATD3
#define LED_7_8_PORT               PORTDbits.RD3
#define LED_7_8_WPU                WPUDbits.WPUD3
#define LED_7_8_SetHigh()            do { LATDbits.LATD3 = 1; } while(0)
#define LED_7_8_SetLow()             do { LATDbits.LATD3 = 0; } while(0)
#define LED_7_8_Toggle()             do { LATDbits.LATD3 = ~LATDbits.LATD3; } while(0)
#define LED_7_8_GetValue()           PORTDbits.RD3
#define LED_7_8_SetDigitalInput()    do { TRISDbits.TRISD3 = 1; } while(0)
#define LED_7_8_SetDigitalOutput()   do { TRISDbits.TRISD3 = 0; } while(0)
#define LED_7_8_SetPullup()      do { WPUDbits.WPUD3 = 1; } while(0)
#define LED_7_8_ResetPullup()    do { WPUDbits.WPUD3 = 0; } while(0)

// get/set LED_5_6 aliases
#define LED_5_6_TRIS               TRISDbits.TRISD4
#define LED_5_6_LAT                LATDbits.LATD4
#define LED_5_6_PORT               PORTDbits.RD4
#define LED_5_6_WPU                WPUDbits.WPUD4
#define LED_5_6_SetHigh()            do { LATDbits.LATD4 = 1; } while(0)
#define LED_5_6_SetLow()             do { LATDbits.LATD4 = 0; } while(0)
#define LED_5_6_Toggle()             do { LATDbits.LATD4 = ~LATDbits.LATD4; } while(0)
#define LED_5_6_GetValue()           PORTDbits.RD4
#define LED_5_6_SetDigitalInput()    do { TRISDbits.TRISD4 = 1; } while(0)
#define LED_5_6_SetDigitalOutput()   do { TRISDbits.TRISD4 = 0; } while(0)
#define LED_5_6_SetPullup()      do { WPUDbits.WPUD4 = 1; } while(0)
#define LED_5_6_ResetPullup()    do { WPUDbits.WPUD4 = 0; } while(0)

// get/set LED_3_4 aliases
#define LED_3_4_TRIS               TRISDbits.TRISD5
#define LED_3_4_LAT                LATDbits.LATD5
#define LED_3_4_PORT               PORTDbits.RD5
#define LED_3_4_WPU                WPUDbits.WPUD5
#define LED_3_4_SetHigh()            do { LATDbits.LATD5 = 1; } while(0)
#define LED_3_4_SetLow()             do { LATDbits.LATD5 = 0; } while(0)
#define LED_3_4_Toggle()             do { LATDbits.LATD5 = ~LATDbits.LATD5; } while(0)
#define LED_3_4_GetValue()           PORTDbits.RD5
#define LED_3_4_SetDigitalInput()    do { TRISDbits.TRISD5 = 1; } while(0)
#define LED_3_4_SetDigitalOutput()   do { TRISDbits.TRISD5 = 0; } while(0)
#define LED_3_4_SetPullup()      do { WPUDbits.WPUD5 = 1; } while(0)
#define LED_3_4_ResetPullup()    do { WPUDbits.WPUD5 = 0; } while(0)

// get/set LED_1_2 aliases
#define LED_1_2_TRIS               TRISDbits.TRISD6
#define LED_1_2_LAT                LATDbits.LATD6
#define LED_1_2_PORT               PORTDbits.RD6
#define LED_1_2_WPU                WPUDbits.WPUD6
#define LED_1_2_SetHigh()            do { LATDbits.LATD6 = 1; } while(0)
#define LED_1_2_SetLow()             do { LATDbits.LATD6 = 0; } while(0)
#define LED_1_2_Toggle()             do { LATDbits.LATD6 = ~LATDbits.LATD6; } while(0)
#define LED_1_2_GetValue()           PORTDbits.RD6
#define LED_1_2_SetDigitalInput()    do { TRISDbits.TRISD6 = 1; } while(0)
#define LED_1_2_SetDigitalOutput()   do { TRISDbits.TRISD6 = 0; } while(0)
#define LED_1_2_SetPullup()      do { WPUDbits.WPUD6 = 1; } while(0)
#define LED_1_2_ResetPullup()    do { WPUDbits.WPUD6 = 0; } while(0)

// get/set LED_PM aliases
#define LED_PM_TRIS               TRISDbits.TRISD7
#define LED_PM_LAT                LATDbits.LATD7
#define LED_PM_PORT               PORTDbits.RD7
#define LED_PM_WPU                WPUDbits.WPUD7
#define LED_PM_SetHigh()            do { LATDbits.LATD7 = 1; } while(0)
#define LED_PM_SetLow()             do { LATDbits.LATD7 = 0; } while(0)
#define LED_PM_Toggle()             do { LATDbits.LATD7 = ~LATDbits.LATD7; } while(0)
#define LED_PM_GetValue()           PORTDbits.RD7
#define LED_PM_SetDigitalInput()    do { TRISDbits.TRISD7 = 1; } while(0)
#define LED_PM_SetDigitalOutput()   do { TRISDbits.TRISD7 = 0; } while(0)
#define LED_PM_SetPullup()      do { WPUDbits.WPUD7 = 1; } while(0)
#define LED_PM_ResetPullup()    do { WPUDbits.WPUD7 = 0; } while(0)

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