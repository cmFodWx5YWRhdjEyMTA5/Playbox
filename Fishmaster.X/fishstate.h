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
    
#define K_TEMPLOWERLIMIT                5  // when sensors report below this, assume something wrong and don't heat     
#define K_MAX_HOTPLATETEMP              43 // when exceed this, shut down
#define K_MIN_HOTPLATETEMPTORESTART     39 // when exceeded, don't restart until at least this
#define K_TARGETWATERTEMP               27 // the target temp for the water
#define K_TARGETWATERTEMPTHREHOLD       00 // the temp away from the target in which the water is fine
    
#define K_MSECONDSINHOUR                36000000    // the number of milliseconds in an hour
#define K_MSECONDSINDAY                 864000000   // the number of milliseconds in a day
    
#define K_SHORTBUTTONPRESSTIME          50     // the time (in ms) that constitues a press
#define K_LONGBUTTONPRESSTIME           1000   // the time (in ms) that constitues a long-press
    
#define K_GAUSSIAN_RED_PEAK             20.0     // the peak value for the gaussian curve for red lighting
#define K_GAUSSIAN_RED_STD              0.15     // the standard deviation for the gaussian curve for red lighting
#define K_GAUSSIAN_RED_MAX              250.0    // the maximum value for the gaussian curve for red lighting
    
#define K_GAUSSIAN_WHITE_PEAK           14.0     // the peak value for the gaussian curve for white lighting
#define K_GAUSSIAN_WHITE_STD            0.25     // the standard deviation for the gaussian curve for white lighting
#define K_GAUSSIAN_WHITE_MAX            300      // the maximum value for the gaussian curve for white lighting

#define K_GAUSSIAN_BLUE_PEAK            7.0      // the peak value for the gaussian curve for blue lighting
#define K_GAUSSIAN_BLUE_STD             0.15     // the standard deviation for the gaussian curve for blue lighting
#define K_GAUSSIAN_BLUE_MAX             250.0    // the maximum value for the gaussian curve for blue lighting
    
// debugging defines 
#define K_DEBUG
//#define K_DEBUG_LED     // will get LED brightness from the potentiometer instead
//#define K_DEBUG_HPT     // will get hot plate temp from to potentiometer instead
//#define K_DEBUG_WT      // will get water temp from the potentiometer instead

// the state structure that all classes share and put their latest data in
static struct t_fishstate {
    volatile uint32_t tick_count;
    uint32_t milliseconds;
#if defined(K_DEBUG_WT) || defined(K_DEBUG_HPT)
    uint16_t potPosition;
#endif
#ifdef K_DEBUG
    uint16_t chipTemp;
#endif
    uint8_t hotPlatePower;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    float waterTemp;
    float hotPlateTemp;
    float intensity;
    bool isLightsOn;
    bool isButtonPress;
    bool isLongButtonPress;
    bool isDemoMode;
};
extern struct t_fishstate FISH_State;

#ifdef K_DEBUG
void FISHSTATE_print(void);
#endif
void FISHSTATE_calcTime(void);


#ifdef	__cplusplus
}
#endif

#endif	/* FISHSTATE_H */

