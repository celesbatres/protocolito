#include "router.h"
#include "frame.h"
#include "lib/list.h"
#include "master.h"
#include "pico/sem.h"
#include "pico/stdio.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "pico/time.h"
#include "slave.h"
#include "hardware/uart.h"
#include "hardware/gpio.h"

// Definición de pines y UART
#define UART_ID uart0
#define BAUD_RATE 115200
#define UART_TX_PIN 0
#define UART_RX_PIN 1

int main (void) {
  // Enable UART
  stdio_init_all ();

  slave_init ();
  master_init ();

  // example of data from Drvier Layer
  // ============================================================
  /*char *data = "I'm Poseidon!";
  struct frame *f = malloc (sizeof *f);
  f->to = 0x5;
  f->from = 0x1;
  f->type = message;
  for (int8_t i = 0; i < 14; i++) {
    f->data[i] = data[i];
  }
  struct receive_elem *elem = malloc(sizeof *elem);
  elem->f = f;

  sem_acquire_blocking (&receive_sema);
  list_push_back (&receive_list, &elem->elem);
  sem_release (&receive_sema);*/

  sleep_ms(1 * 1000);
  // ============================================================
  char data[99] = {0};
  int buffer_index=0;

  while (1) {
    //struct receive_elem *elem = NULL;
    char c = uart_getc(UART_ID);

        if (c == '\n')
        {
            data[buffer_index] = '\0';
            buffer_index = 0;
            printf("Recibí del Driver: %s", data);
            // create_frame(data);
        }
        else
        {
            data[buffer_index++] = c;
        }
  }

  
}

void create_frame(char data[]){
    struct frame *f = malloc(sizeof *f);
    f->to = 0x01;
    f->from = 0x06;
    f->length = strlen(data) + 1;
    f->header_checksum = ((f->to + f->from + f->length) ^ 0xFF) + 0x1;    // checksum is ALWAYS calculated the same

    f->data = malloc(f->length);
    memcpy(f->data, data, f->length);

    /*char send_data[200];
    sprintf(send_data, "{ ID: %d Tipo: %d To: %d From: %d Data: %s }", f->id, f->type, f->to, f->from, f->data);
    uart_puts(UART_ID, send_data);*/

    master_propagate(f);

    free(f->data);
    free(f);
  }