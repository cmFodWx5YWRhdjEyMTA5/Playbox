/* 
 * File:   fishoutput.h
 * Author: douglasbrain
 *
 * Created on July 18, 2017, 9:03 AM
 */

#ifndef FISHOUTPUT_H
#define	FISHOUTPUT_H

#include <stdint.h>
#include <stdbool.h>

#ifdef	__cplusplus
extern "C" {
#endif
    void FISHOUTPUT_Initialize(void);
    
    void FISHOUTPUT_process(void);
    
    void FISHOUTPUT_setHotPlatePower(uint8_t);

#ifdef	__cplusplus
}
#endif

#endif	/* FISHOUTPUT_H */

