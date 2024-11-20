//=================================================================================//

#include "slave.h"
#include "frame.h"
#include "master.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include "hardware/gpio.h"
#include "hardware/spi.h"
#include "lib/list.h"
#include "pico/sem.h"
#include "pico/stdlib.h"
#include "pico/sem.h"
#include <string.h>

#define UART_ID uart0
//=================================================================================//

semaphore_t receive_sema;     // syncronization
struct list receive_list;

//=================================================================================//

void spiReceiveISR () {
  // 1. Crear buffers
  uint8_t header[4];
  uint8_t *data;

  // 2. leer los bytes del header
  spi_read_blocking (spi0, 0, header, 4);

  // 3. validar el checksum (ver sección anterior)
  // ...

  // 4. crear frame
  struct frame *f = malloc(sizeof *f);
  f->to = header[0];
  f->from = header[1];
  f->length = header[2];
  f->header_checksum = header[3];

  // 5. leer data
  f->data = malloc(f->length);
  spi_read_blocking (spi0, 0, f->data, f->length);

  uint8_t valid = header[0] + header[1] + header[2] + header[3];
  if (valid == 0) {
    if(f->to == 0x64) {
      // uart_puts(UART_ID, f->data);
      uart_write_blocking(UART_ID, f->data, f->length);
    } else if(f->from == 0x64){

    }else {
      master_propagate(f);
    }
  } else {
    // checksum ERROR
  }
          
  free(f->data);
  free(f);
}

//=================================================================================//

void slave_init (void) {
  // enable SPI0 at 10kHz
  spi_init (spi0, BAUDRATE);
  spi_set_slave (spi0, true);

  // assign SPI functions to default SPI pint.
  // for more info read RP2040 Datasheet(2.19.2. Function Select).
  gpio_set_function (16, GPIO_FUNC_SPI);
  gpio_set_function (17, GPIO_FUNC_SPI);
  gpio_set_function (18, GPIO_FUNC_SPI);
  // gpio_set_function (19, GPIO_FUNC_SPI);

  // Enable the RX FIFO interrupt (RXIM)
  spi0_hw->imsc = 1 << 2;

  // Enable the SPI interrupt
  irq_set_enabled (SPI0_IRQ, 1);

  // Attach the interrupt handler
  irq_set_exclusive_handler (SPI0_IRQ, spiReceiveISR);

  // init list
  sem_init (&receive_sema, 1, 1);
  list_init (&receive_list);
}

