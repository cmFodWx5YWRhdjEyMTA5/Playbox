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