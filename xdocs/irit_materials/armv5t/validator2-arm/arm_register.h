#ifndef LEON_REGISTER_H
#define LEON_REGISTER_H

#include <stdint.h>
#include GLISS_API_H

/* the python script, as given, cannot "render" a "moving" register
 * like the banked ones of the ARM */

uint32_t get_arm_reg(PROC(_state_t) * st, unsigned int idx);

void set_arm_reg(PROC(_state_t) * st, unsigned int idx, uint32_t val);



/* return the ID of the gliss reg IDed by the text description and the index */
void get_gliss_reg_addr(char *desc, PROC(_state_t) * st, int *bank, int *idx);

/* get the value of the register #idx in reg_infos[] */
uint64_t get_gliss_reg(PROC(_state_t) * st, int idx);

#endif /* LEON_REGISTER_H */
