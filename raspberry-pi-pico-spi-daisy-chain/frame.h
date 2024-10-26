#ifndef __FRAME_H__
#define __FRAME_H__

#include <stdint.h>

#define BAUDRATE      10 * 1000
#define CHUNK_SIZE    1

/* frame definitions */
enum frame_type { message, ACK };
struct frame {
  uint8_t length; 
  uint8_t header_checksum;    
  uint8_t to;                 // assigned in class
  uint8_t from;               // assigned in class
  char *data;              // actual data   

};

#endif // !FRAME_H

