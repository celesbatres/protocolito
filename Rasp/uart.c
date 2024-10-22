#include "pico/stdlib.h"
#include "hardware/uart.h"
#include <stdio.h>
#include <string.h>

// Configuración UART
#define UART_ID uart0
#define BAUD_RATE 9600//115200
#define UART_TX_PIN 0
#define UART_RX_PIN 1
#define MAX_BUFFER_SIZE 100  

void uart_read_string(uart_inst_t *uart, char *buffer, size_t max_len) {
    size_t index = 0;
    char recv_char;

    while (index < max_len - 1) {
        if (uart_is_readable(uart)) {
            recv_char = uart_getc(uart);

            // printf("%c", recv_char); 
            if (recv_char == '\n') {
                break;
            } 

            buffer[index++] = recv_char;
        }
    }

    buffer[index] = '\0';
}

int main() {
    uart_init(UART_ID, BAUD_RATE);
    gpio_set_function(UART_TX_PIN, GPIO_FUNC_UART); // GPIO 0 para TX
    gpio_set_function(UART_RX_PIN, GPIO_FUNC_UART);  // GPIO 1 para RX

    // 8 bits de datos, sin paridad y 1 bit de stop
    uart_set_format(UART_ID, 8, 1, UART_PARITY_NONE);

    uart_set_fifo_enabled(UART_ID, true);

    char buffer[MAX_BUFFER_SIZE];

    while (true) {
        memset(buffer, 0, sizeof(buffer));

        uart_read_string(UART_ID, buffer, MAX_BUFFER_SIZE);

        printf("\nMensaje que recibió UART: %s\n", buffer);
    }

    return 0;
}
