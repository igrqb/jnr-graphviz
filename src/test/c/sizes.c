/* Prints sizeof values the Java tests pass to aginit and parsePackModeInfo. */
#include <graphviz/cgraph.h>
#include <graphviz/gvc.h>
#include <graphviz/pack.h>
#include <stdio.h>

int main(void) {
  printf("Agraphinfo_t %zu\n", sizeof(Agraphinfo_t));
  printf("Agnodeinfo_t %zu\n", sizeof(Agnodeinfo_t));
  printf("Agedgeinfo_t %zu\n", sizeof(Agedgeinfo_t));
  printf("Agrec_t %zu\n", sizeof(Agrec_t));
  printf("pack_info %zu\n", sizeof(pack_info));
  return 0;
}
