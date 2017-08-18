/* 
 * File:   fishinput.h
 * Author: douglasbrain
 *
 * Created on July 18, 2017, 9:03 AM
 */

#ifndef FISHINPUT_H
#define	FISHINPUT_H

#include <stdint.h>
#include <stdbool.h>

#ifdef	__cplusplus
extern "C" {
#endif
    void FISHINPUT_Initialize(void);
    
    void FISHINPUT_process(void);
    
    bool FISHINPUT_isPressed(void);

    bool FISHINPUT_isLongPressed(void);
    
    float FISHINPUT_getHotPlateTemp(void);
    
    float FISHINPUT_getWaterTemp(void);
    
#ifdef K_DEBUG
    float FISHINPUT_getChipTemp(void);
    
    uint16_t FISHINPUT_getPotPosition(void);
#endif
    void FISHINPUT_longButtonPressHandled(void);

#ifdef	__cplusplus
}
#endif

#endif	/* FISHINPUT_H */

