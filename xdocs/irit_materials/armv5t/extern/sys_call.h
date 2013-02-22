#ifndef GLISS_SYS_CALL_H
#define GLISS_SYS_CALL_H

#if defined(__cplusplus)
extern "C" {
#endif

#define GLISS_SYS_CALL_STATE
#define GLISS_SYS_CALL_INIT(s)
#define GLISS_SYS_CALL_DESTROY(s)

void swi_impl(int code);

#if defined(__cplusplus)
}
#endif


#endif /* GLISS_SYS_CALL_H */
