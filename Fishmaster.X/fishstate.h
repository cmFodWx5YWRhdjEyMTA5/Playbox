/* 
 * File:   fishstate.h
 * Author: douglasbrain
 *
 * Created on July 18, 2017, 9:20 AM
 */

#ifndef FISHSTATE_H
#define	FISHSTATE_H

#include <stdint.h>
#include <stdbool.h>

#ifdef	__cplusplus
extern "C" {
#endif
    
// defines that are helpful
#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))
    
#define K_MAX_HOTPLATETEMP              90 // when exceed this, shut down
#define K_MIN_HOTPLATETEMPTORESTART     50 // when exceeded, don't restart until at least this
#define K_TARGETWATERTEMP               49 // the target temp for the water
#define K_TARGETWATERTEMPTHREHOLD       00 // the temp in which the water is fine
    
#define K_MSECONDSINHOUR                 36000000    // the number of milliseconds in an hour
#define K_MSECONDSINDAY                  864000000   // the number of milliseconds in a day
    
#define K_SHORTBUTTONPRESSTIME          200    // the time (in ms) that constitues a press
#define K_LONGBUTTONPRESSTIME           2000   // the time (in ms) that constitues a long-press
    
// debugging defines
//#define K_DEBUG_HPT     // will get hot plate temp from to potentiometer instead
//#define K_DEBUG_WT      // will get water temp from the potentiometer instead

// the state structure that all classes share and put their latest data in
static struct t_fishstate {
    volatile uint32_t tick_count;
    uint32_t milliseconds;
    uint16_t potPosition;
    uint8_t hotPlatePower;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    float waterTemp;
    float hotPlateTemp;
    float chipTemp;
    bool isLightsOn;
    bool isButtonPress;
    bool isLongButtonPress;
    bool isSlave;
};
extern struct t_fishstate FISH_State;

void FISHSTATE_print(void);

void FISHSTATE_calcTime(void);


#ifdef	__cplusplus
}
#endif

#endif	/* FISHSTATE_H */

