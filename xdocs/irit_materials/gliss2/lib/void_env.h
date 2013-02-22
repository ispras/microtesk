#ifndef GLISS_LINUX_ENV_H
#define GLISS_LINUX_ENV_H

#include "api.h"
#include "loader.h"

#if defined(__cplusplus)
    extern  "C" {
#endif


/* module name: env */

#define GLISS_ENV_STATE
#define GLISS_ENV_INIT(s)
#define GLISS_ENV_DESTROY(s)


/* system initialization (used internally during platform and state initialization) */
void gliss_stack_fill_env(gliss_loader_t *loader, gliss_platform_t *platform,  gliss_env_t *env);
void gliss_registers_fill_env(gliss_env_t *env, gliss_state_t *state);
void gliss_set_brk(gliss_platform_t *pf, gliss_address_t address);

#if defined(__cplusplus)
}
#endif

#endif /* GLISS_LINUX_ENV_H */
