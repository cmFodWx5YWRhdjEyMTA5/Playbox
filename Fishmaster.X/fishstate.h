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
#define K_MAX_HOTPLATETEMP              90 // when exceed this, shut down
#define K_MIN_HOTPLATETEMPTORESTART     50 // when exceeded, don't restart until at least this
#define K_TARGETWATERTEMP               49 // the target temp for the water
#define K_TARGETWATERTEMPTHREHOLD       00 // the temp in which the water is fine
    
// debugging defines
//#define K_DEBUG_HPT     // will get hot plate temp from to potentiometer instead
//#define K_DEBUG_WT      // will get water temp from the potentiometer instead

// the state structure that all classes share and put their latest data in
static struct t_fishstate {
    uint32_t tick_count;
    uint16_t potPosition;
    uint8_t hotPlatePower;
    float waterTemp;
    float hotPlateTemp;
    float chipTemp;
};
extern struct t_fishstate FISH_State;

void FISHSTATE_print(void);


#ifdef	__cplusplus
}
#endif

#endif	/* FISHSTATE_H */

