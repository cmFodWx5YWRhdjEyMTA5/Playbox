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
    
    void FISHINPUT_longButtonPressHandled(void);

#ifdef	__cplusplus
}
#endif

#endif	/* FISHINPUT_H */

