
#include "rtc.h"
#include "mcc_generated_files/i2c.h"
#include "builddefs.h"
#include <stdio.h>
#include <stdlib.h>

struct t_rtcstate RTC_State;

//http://ww1.microchip.com/downloads/en/DeviceDoc/20002266F.pdf
#define MCP79410_RETRY_MAX  100  // define the retry count
#define MCP79410_ADDRESS    0b1101111 // RTC device address

#define MCP79410_RAWWAITING 1000 // the number of times we wait for the ack

#define MCP79410_ADDRESS_SEC    0x0     // the memory address for the seconds
#define MCP79410_ADDRESS_MIN    0x1     // the memory address for the minutes
#define MCP79410_ADDRESS_HOUR   0x2     // the memory address for the hour
#define MCP79410_ADDRESS_WKDY   0x3     // the memory address for the week day
#define MCP79410_ADDRESS_DAY    0x4     // the memory address for the day
#define MCP79410_ADDRESS_MONTH  0x5     // the memory address for the month
#define MCP79410_ADDRESS_YEAR   0x6     // the memory address for the year
#define MCP79410_ADDRESS_CTRL   0x7     // the RTC control reg for settings

#define MCP79410_CTRL_BITS      0x0     // the state we want the RTC to be in
#define MCP79410_WKDY_CTRL_BITS 0b00101000 // the state of the WKDY ctrl bits we want

void RTC_Initialise(void)
{
    // initialise the data on this class, this will properly initialise the
    // data on the RTC chip on the first call to get the current time
    RTC_State.time_hours = 0;
    RTC_State.time_minutes = 0;
    RTC_State.time_seconds = 0;
    // the ST bit is important as it tells us if the chip is running
    RTC_State.ST_BITSET = 0;
    // start the index at zero
    RTC_State.time_getIndex = 0;
    
    //TRISC = 0xff; // all inputs ; SDA & SCL = inputs 
    // TRISC=0xdf/d8 ; SCL=C3 , SDA=C4 = IN ; SDO(SPI)=C5=OUT 
    SSP1ADD = 0x63; // baud rate = 100 khz ; 10mhz:100 = 100 khz
    SSP1STAT = 0x80; // slew rate dis, SMBUS disabled  
    SSP1CON1 = 0x28; // enable module, I2C master SSP1ADD=baud rate
    SSP1CON2 = 0x00;
    
    // first things first - setup the oscillator by clearing the control bits
    RTC_Write(MCP79410_ADDRESS_CTRL, MCP79410_CTRL_BITS);
    printf("RTC Control BITS set\r\n");
    // now, we don't really care about the day of the week but we do care
    // about the other control bits in this address, the battery status and
    // the oscillator state, set these up now each time
    //RTC_Write(MCP79410_ADDRESS_WKDY, MCP79410_WKDY_CTRL_BITS);
    
    //TODO Remove this, we just want to do this when the clock hasn't been
    //initialised, ie ST bit is set to zero
    if (!RTC_SetCurrentDate()) {
        printf("Failed to set the date correctly\r\n");
    }
}

void RTC_Print(void)
{
    // print out the current time for debugging purposes
    printf("RTC%d:%2d:%2d:%2d\r\n", 
            RTC_State.ST_BITSET,
            RTC_State.time_hours,
            RTC_State.time_minutes,
            RTC_State.time_seconds);
}

bool RTC_ReadTime(void)
{
    // call this function periodically to read and update the time in our
    // RTC_State. We will roll over a counter to call seconds, minutes, hours
    // in that order. Setting the RTC chip if required
    
    // first is the special case for seconds
    if (RTC_State.time_getIndex == 0) {
        // get the seconds and only proceed if successful
        if (RTC_ReadSeconds(true)) {
            // this was good, call minutes next time
            ++RTC_State.time_getIndex;
        }
    }
    else {
        // call the next one
        switch(RTC_State.time_getIndex) {
            case 0:
                // should never be here, but for good code, let's go to seconds
                RTC_ReadSeconds(false);
                break;
            case 1:
                // read the minutes
                RTC_ReadMinutes();
                break;
            case 2:
                // read hours
                RTC_ReadHours();
                break;
            default:
                // shouldn't be here either, reset
                break;
        }
        // increment the counter
        if (++RTC_State.time_getIndex > 2) {
            // rolled over the number of functions, start again
            RTC_State.time_getIndex = 0;
        }
    }
    // return the state of the ST bit after all this
    return RTC_State.ST_BITSET;
}
#ifdef MICROCHIPI2CREADNOTWORKING
bool RTC_Read(uint8_t bitAddress, uint8_t* readBuffer)
{
    // put ourselves in the sending state
    SSPCON2bits.SEN = 1;
    // wait for the RTC chip to ack by resetting this
    uint32_t waitCounter = 0;
    // wait for our ack to this setting of the start status
    while (++waitCounter < MCP79410_RAWWAITING && SSPCON2bits.SEN == 1);
    if (SSPCON2bits.SEN != 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // set the control code to the address of the device with a zero to indicate
    // that we are sending a writing request...
    SSP1BUF = MCP79410_ADDRESS << 1;
    if (RTC_WaitForRAWWriteAck() == 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // now set the address that we want to read data from
    SSP1BUF = bitAddress;
    if (RTC_WaitForRAWWriteAck() == 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // now set the resend data bit to send the read request
    SSP1CON2bits.RSEN = 1;
    waitCounter = 0;
    while (++waitCounter < MCP79410_RAWWAITING && SSPCON2bits.RSEN == 1);
    if (SSPCON2bits.RSEN != 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // now we can send the data request, first the control code with a 1 at the end
    SSP1BUF = MCP79410_ADDRESS << 1;
    SSP1BUF |= 0b00000001;
    if (RTC_WaitForRAWWriteAck() == 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // now set the RCEN data bit
    SSP1CON2bits.RCEN = 1;
    waitCounter = 0;
    while (++waitCounter < MCP79410_RAWWAITING && SSPCON2bits.RCEN == 1);
    if (SSPCON2bits.RCEN != 0) {
        // we didn't get the ack for this resend - quit
        return 0;
    }
    // here we now have the data
    *readBuffer = SSP1BUF;

    // and close the read request, first set the MASTER NOACK bit  
    SSP1CON2bits.ACKDT = 1;
    SSP1CON2bits.ACKEN = 1;
    waitCounter = 0;
    while (++waitCounter < MCP79410_RAWWAITING && SSPCON2bits.ACKEN == 1);
    if (SSPCON2bits.ACKEN != 0) {
        // we didn't get the ack for this close - quit
        //TODO this is where our get of data seems to fail
        return 0;
    }
    SSP1CON2bits.PEN = 1;
    waitCounter = 0;
    while (++waitCounter < MCP79410_RAWWAITING && SSPCON2bits.PEN == 1);
    if (SSPCON2bits.PEN != 0) {
        // we didn't get the ack for this close - quit
        return 0;
    }
    // here? return success then
    return 1;
}
#else
bool RTC_Read(uint8_t bitAddress, uint8_t* readBuffer)
{
    // using the Microchip generated code, let's find the data for the specified
    // address and return the value. We are just doing one at a time as there
    // is no rush for the time and we can have robust code instead of rapid
    // calling of the I2C interface
    I2C_TRANSACTION_REQUEST_BLOCK transactionRequests[2];

    // Build a transaction to write the address of the data we required
    I2C_MasterWriteTRBBuild(&transactionRequests[0],
                            &bitAddress,
                            1,  // length of one - just doing one at a time
                            MCP79410_ADDRESS);
    // Now build the transaction to ask for this data, the address we set
    I2C_MasterReadTRBBuild(&transactionRequests[1],
                           readBuffer,
                           1,  // again just one at a time - the data at address
                           MCP79410_ADDRESS);

    // now we can run through the loop sending the data and checking our status
    uint16_t timeOut = 0;
    I2C_MESSAGE_STATUS status = I2C_MESSAGE_PENDING;
    bool result = 0;
    while (status != I2C_MESSAGE_FAIL) {
        // insert the transactions into the I2C queue that Microchip created
        I2C_MasterTRBInsert(2, transactionRequests, &status);

        // wait for the message to be sent or status has changed.
        uint16_t waitCounter = 0;
        while(status == I2C_MESSAGE_PENDING) {
            // while the status is pending we want to wait a little
            // but not forever - that would be bad code
            if (++waitCounter > 1000) {
                // wait a thousand clock cycles, that should be enough
                break;
            }
        }
        // have either timed out our wait or the message is no longer
        // pending, check here and deal with the result
        if (status == I2C_MESSAGE_COMPLETE) {
            // this data read was a success
            result = 1;
            break;
        }

        // if we are here then the status is either
        // I2C_MESSAGE_ADDRESS_NO_ACK,
        // or I2C_DATA_NO_ACK,
        // The device may be busy and needs more time for the last
        // write so we can retry writing the data, this is why we
        // use a while loop here

        // check for max retry and skip this byte
        if (++timeOut > MCP79410_RETRY_MAX) {
            // exceeded the total number of retries, forget it... fail
            break;
        }
    }
    // and return the result of our getting of this data, the data will be
    // set in the passed buffer
    return result;
}
#endif //MICROCHIPI2CNOTWORKING

#ifdef MICROCHIPI2CWRITENOTWORKING
bool RTC_Write(uint8_t bitAddress, uint8_t writeValue)
{
    // for some unknown painful reason the microchip writing code is not
    // working, so here is some more RAW code for doing the same job
    
    // set the starting status
    SSPCON2bits.SEN = 1;
    // wait for the RTC chip to ack by resetting this
    uint32_t waitCounter = 0;
    // wait for our ack to this setting of the start status
    while (++waitCounter < MCP79410_RAWWAITING && SSPCON2bits.SEN == 1);
    if (SSPCON2bits.SEN != 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // set the control code to the address of the device with a zero to indicate
    // that we are sending a writing request...
    SSP1BUF = MCP79410_ADDRESS << 1;
    if (RTC_WaitForRAWWriteAck() == 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // now set the address that we want to write data to
    SSP1BUF = bitAddress;
    if (RTC_WaitForRAWWriteAck() == 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // now set the data we want to set on this address
    SSP1BUF = writeValue;
    if (RTC_WaitForRAWWriteAck() == 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // and finally close the request
    SSP1CON2bits.PEN = 1;
    // and wait for this to become zero again
    waitCounter = 0;
    // wait for our ack to this setting of the start status
    while (++waitCounter < MCP79410_RAWWAITING && SSPCON2bits.PEN == 1);
    if (SSP1CON2bits.PEN != 0) {
        // we didn't get the ack - quit
        return 0;
    }
    // this is the success area
    return 1;
}

bool RTC_WaitForRAWWriteAck(void)
{
    uint32_t waitCounter = 0;
    bool isAck = 0;
    while (++waitCounter < MCP79410_RAWWAITING) {
        if ((SSPSTATbits.R_nW != 1) || (SSPSTATbits.BF != 1) || (SSPCON2bits.ACKSTAT != 1)) {
            // this is an ack, something has changed
            isAck = 1;
            break;
        }
    }
    return isAck;
}
#else
bool RTC_Write(uint8_t bitAddress, uint8_t writeValue)
{
    // using the Microchip generated code, let's set the data for the specified
    // address. We are just doing one at a time as there is no rush to set the
    // time and we can have robust code instead of rapid calling of the 
    // I2C interface
    I2C_TRANSACTION_REQUEST_BLOCK transactionRequests[2];

    // Build a transaction to write the address of the data we want to set
    I2C_MasterWriteTRBBuild(&transactionRequests[0],
                            &bitAddress,
                            1,  // length of one - just doing one at a time
                            MCP79410_ADDRESS);
    // Now build the transaction to set this data at the address we set
    I2C_MasterWriteTRBBuild(&transactionRequests[1],
                            &writeValue,
                            1,  // again just one at a time
                            MCP79410_ADDRESS);

    // now we can run through the loop sending the data and checking our status
    uint16_t timeOut = 0;
    I2C_MESSAGE_STATUS status = I2C_MESSAGE_PENDING;
    bool result = 0;
    while (status != I2C_MESSAGE_FAIL) {
        // insert the transactions into the I2C queue that Microchip created
        I2C_MasterTRBInsert(2, transactionRequests, &status);

        // wait for the message to be sent or status has changed.
        uint16_t waitCounter = 0;
        while(status == I2C_MESSAGE_PENDING) {
            // while the status is pending we want to wait a little
            // but not forever - that would be bad code
            if (++waitCounter > 1000) {
                // wait a thousand clock cycles, that should be enough
                break;
            }
        }
        // have either timed out our wait or the message is no longer
        // pending, check here and deal with the result
        if (status == I2C_MESSAGE_COMPLETE) {
            // this data write was a success
            result = 1;
            break;
        }

        // if we are here then the status is either
        // I2C_MESSAGE_ADDRESS_NO_ACK,
        // or I2C_DATA_NO_ACK,
        // The device may be busy and needs more time for the last
        // write so we can retry writing the data, this is why we
        // use a while loop here

        // check for max retry and skip this byte
        if (++timeOut > MCP79410_RETRY_MAX) {
            // exceeded the total number of retries, forget it... fail
            break;
        }
    }
    // and return the result of our setting of this data
    return result;
}
#endif //MICROCHIPI2CNOTWORKING
bool RTC_SetCurrentDate(void)
{
    // set the current date and time, being sure to set the ST bit in the seconds
    // to start the RTC chip up and running
    // first let's set the date - we want the seconds to be last of course
    // as this will start the counting from the date we just spent ages setting
    uint16_t year = BUILD_YEAR - 2000;
    bool isLeap = false;
    uint16_t month = BUILD_MONTH;
    uint16_t day = BUILD_DAY;
    uint16_t hours = BUILD_HOUR;
    uint16_t minutes = BUILD_MIN;
    uint16_t seconds = BUILD_SEC;
    
    // set the year, bits 7-4 are the 'tens' just 0-9 then
    uint16_t tensValue = (uint16_t)(year / 10);
    uint8_t rtcYear = (0b1111 & tensValue) << 4;
    // now the 'ones' which are bits 3-0
    rtcYear |= (0b1111) & (year - (tensValue * 10));
    
    // also the month, similar but with leap year built in
    uint8_t rtcMonth = 0;
    if (isLeap) {
        // bit 5 has to be 1 if a leap year
        rtcMonth |= 0b00100000;
    }
    if (month > 10) {
        // bit 4 is the 'tens' digit
        rtcMonth |= 0b00010000;
    }
    // and then bits 3-0 as the 'ones' digit
    rtcMonth |= (0b1111) & month;
    
    // now the day bits 5-4 are the 'tens' digit
    tensValue = (uint16_t)(day / 10);
    uint8_t rtcDay = (0b11 & tensValue) << 4;
    // now the 'ones' which are bits 3-0
    rtcDay |= (0b1111) & (day - (tensValue * 10));
    
    // hours, let's do as 24hr clock for simplicity, leave bit 6 as 0 to be 
    // in 24 hour format - bits 5-4 are the 'tens' of the hour
    tensValue = (uint16_t)(hours / 10);
    uint8_t rtcHours = (0b11 & tensValue) << 4;
    // now the 'ones' which are bits 3-0
    rtcHours |= (0b1111) & (hours - (tensValue *10));
    
    // minutes are simple, 'tens' are bits 6-4
    tensValue = (uint16_t)(minutes / 10);
    uint8_t rtcMinutes = (0b111 & tensValue) << 4;
    // now the 'ones' which are bits 3-0
    rtcMinutes |= (0b1111) & (minutes - (tensValue * 10));
    
    // seconds we could leave but are vital to set the ST bit, which is bit 7
    uint8_t rtcSeconds = 0b10000000;
    // the rest is simple, 'tens' are bits 6-4
    tensValue = (uint16_t)(seconds / 10);
    rtcSeconds |= (0b111 & tensValue) << 4;
    // now the 'ones' which are bits 3-0
    rtcSeconds |= (0b1111) & (seconds - (tensValue * 10));
    
    // I know we did this in initialise but if the date isn't working
    // then maybe this failed, keep trying to set this important control bit
    bool result = RTC_Write(MCP79410_ADDRESS_CTRL, MCP79410_CTRL_BITS);
    // and the control bits in the weekday field, setting the oscillator on
    // and the battery backup to be on
    //result = RTC_Write(MCP79410_ADDRESS_WKDY, MCP79410_WKDY_CTRL_BITS);
    // and stop the timer from running now by setting the ST BIT to zero
    result = RTC_Write(MCP79410_ADDRESS_SEC, 0x0);
    if (result) {
        // have stopped the timer, wait for it to actually stop now then
        RTC_State.ST_BITSET = 0;
        // so now we have to wait for the OSCRUN bit in the WKDY time to clear
        uint8_t weekday;
        uint16_t retryCounter = 0;
        while (++retryCounter < MCP79410_RETRY_MAX) {
            if (RTC_Read(MCP79410_ADDRESS_WKDY, &weekday) == 0) {
                // failed to read, forget it then
                break;
            }
            else {
                // have the data, is the OSCRUN bit reset?
                if ((weekday & 0b00100000) == 0) {
                    // OSCRUN Reset
                    break;
                }
            }
        }
    }
    // now we have all the data setup and initialised ready, we can set it
    // one at a time for the I2C chip to be happy with the starts and stops
    if (result) {
        // set the year
        result = RTC_Write(MCP79410_ADDRESS_YEAR, rtcYear);
    }
    else {
        printf("Failed to stop the ST-BIT to set the time -- ");
    }
    if (result) {
        // set the month
        result = RTC_Write(MCP79410_ADDRESS_MONTH, rtcMonth);
    }
    else {
        printf("Failed to set year -- ");
    }
    if (result) {
        // set the day
        result = RTC_Write(MCP79410_ADDRESS_DAY, rtcDay);
    }
    else {
        printf("Failed to set month -- ");
    }
    if (result) {
        // set the hour
        result = RTC_Write(MCP79410_ADDRESS_HOUR, rtcHours);
    }
    else {
        printf("Failed to set day -- ");
    }
    if (result) {
        // set the minutes
        result = RTC_Write(MCP79410_ADDRESS_MIN, rtcMinutes);
    }
    else {
        printf("Failed to set hour -- ");
    }
    
    if (!result)  {
        printf("Failed to set minutes -- ");
    }
    
    
    // and the control bits in the weekday field, setting the oscillator on
    // and the battery backup to be on
    //result = RTC_Write(MCP79410_ADDRESS_WKDY, MCP79410_WKDY_CTRL_BITS);
    // and finally the oh-so-important seconds that contains the ST bit
    // always set this to try and keep it running at all times
    result = RTC_Write(MCP79410_ADDRESS_SEC, 0x80);
    if (result) {
        RTC_State.ST_BITSET = 1;
    }
    // and finally return the result of this
    return result;
}

bool RTC_ReadSeconds(bool isSetIfFail)
{
    // this is in a special function to be called first as if they are not
    // set properly then we need to check the ST flag is also set and the
    // RTC is running. When delivered the RTC ST flag is 0 and the clock
    // doesn't count. So we need to set it running!...
    // first read the seconds bit (0x0) from the RTC
    uint8_t seconds = 0;
    bool result = 0;
    if (RTC_Read(MCP79410_ADDRESS_SEC, &seconds) != 1) {
        // this failed for some reason
        printf("Getting the RTC value for seconds failed.\r\n");
    }
    else {
        // so get the seconds, bit 7 is the ST bit
        uint8_t secondsST   = (seconds & 0b10000000) >> 7;
        // bits 6-4 are the 'tens' digit
        uint8_t secondsTens = (seconds & 0b01110000) >> 4;
        // first 4 bits are the 'ones' digit
        uint8_t secondsOnes = seconds & 0b00001111;
        if (secondsST == 0) {
            // the ST bit is zero, not one, the cock is not set, so let us set
            // the clock by initialising the bit to be a one
            if (isSetIfFail) {
                // we want to set this data if the ST was not set
                // so set the current data and time to be the compile time
                // which should be very close as running it to test it works
                // after putting new code on?
                if (RTC_SetCurrentDate()) {
                    // setting the date was a success, try to get the seconds
                    // one more time, but don't set if fail then we might get in
                    // to an infinite recursion loop here
                    //TODO Can't call this again as compiler complains )O:
                    //:: warning: (1273) Omniscient Code Generation not available in Free mode
                    //result = RTC_ReadSeconds(false);
                }
            }
        }
        else {
            // the ST bit is one, seconds are set, this is fine
            // set this data in our status
            RTC_State.time_seconds = secondsOnes + (secondsTens * 10);
            // set the ST bit state on the state
            RTC_State.ST_BITSET = secondsST == 0 ? 0 : 1;
            // and return that the time is running and retrieved
            result = 1;
        }
    }
    // return the result of this retrieval
    return result;
}

void RTC_ReadMinutes(void)
{
    // this is a little easier than seconds as we just want the data
    // from the RTC chip and decode it to the minutes we have currently
    uint8_t minutes = 0;
    if (RTC_Read(MCP79410_ADDRESS_MIN, &minutes) != 1) {
        // this failed for some reason
        printf("Getting the RTC value for minutes failed.\r\n");
    }
    else {
        // so get the minutes, bits 6-4 are the 'tens' digit
        uint8_t minutesTens = (minutes & 0b01110000) >> 4;
        // first 4 bits are the 'ones' digit
        uint8_t minutesOnes = minutes & 0b00001111;
        // and set this in the state
        RTC_State.time_minutes = minutesOnes + (minutesTens * 10);
    }
}

void RTC_ReadHours(void)
{
    // this is a little easier than seconds as we just want the data
    // from the RTC chip and decode it to the hours we have currently
    // but harder as we have to deal with 12/24 hr and am/pm annoyingly
    uint8_t hours = 0;
    if (RTC_Read(MCP79410_ADDRESS_HOUR, &hours) != 1) {
        // this failed for some reason
        printf("Getting the RTC value for hours failed.\r\n");
    }
    else {
        // so get the hours
        // bit number 6 is 12/24hr format (1 is 12 hour, 0 is 24 hour)
        bool isAmPmHours = hours & 0b01000000 >> 6;
        if (isAmPmHours) {
            // this is separated into am and pm, so bit 5 1 is PM, 0 is AM
            bool isPm = hours & 0b00100000 >> 5;
            // bit 4 is the 'tens' digit
            uint8_t hoursTens = (hours & 0b00010000) >> 4;
            // bits 3-0 is the 'ones' digit
            uint8_t hoursOnes = hours & 0b00001111;
            // so calculate the hours from all this
            RTC_State.time_hours = hoursOnes + ((isPm ? 12 : 0) + (hoursTens * 10));
        }
        else {
            // this is the 24 hour clock format, so has tens and ones again
            //bits 5-4 are the 'tens' digit
            uint8_t hoursTens = (hours & 0b00110000) >> 4;
            // first 4 bits are the 'ones' digit
            uint8_t hoursOnes = hours & 0b00001111;
            // and set this in the state
            RTC_State.time_hours = hoursOnes + (hoursTens * 10);
        }
    }
}