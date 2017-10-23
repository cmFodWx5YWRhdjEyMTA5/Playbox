/* 
 * File:   rtc.h
 * Author: douglasbrain
 *
 * Created on July 18, 2017, 9:20 AM
 */

#ifndef RTC_H
#define	RTC_H

#include <stdint.h>
#include <stdbool.h>

#ifdef	__cplusplus
extern "C" {
#endif
        
// the state structure that we can store our RTC inside for reference
// by others
static struct t_rtcstate {
    uint8_t time_seconds;
    uint8_t time_minutes;
    float time_hours;
    uint8_t time_hour;
    bool    ST_BITSET;
    uint8_t time_getIndex;
};
extern struct t_rtcstate RTC_State;

// If this define is set then the microchip I2C code isn't writing for
// some reason and we can use the RAW code that we know does work
#define MICROCHIPI2CREADNOTWORKING
#define MICROCHIPI2CWRITENOTWORKING

void RTC_Initialise(void);

bool RTC_Read(uint8_t bitAddress, uint8_t* readBuffer);

bool RTC_Write(uint8_t bitAddress, uint8_t writeValue);
//#if defined MICROCHIPI2CREADNOTWORKING || defined MICROCHIPI2CWRITENOTWORKING
bool RTC_WaitForRAWWriteAck(void);
//#endif

float RTC_HoursFromTime(void);

bool RTC_ReadTime(void);

bool RTC_ReadSeconds(bool isSetIfFail);

void RTC_ReadMinutes(void);

void RTC_ReadHours(void);

void RTC_ReadHoursMinutes(void);

bool RTC_SetCurrentDate(void);

void RTC_WaitForOSCRUN(bool isRunRequired);

bool RTC_IncrementHour(void);

bool RTC_setDate(uint16_t year, uint16_t month, uint16_t day, uint16_t hours, uint16_t minutes, uint16_t seconds);


#ifdef	__cplusplus
}
#endif

#endif	/* FISHSTATE_H */

