#ifndef TELEMETRY_H
#define TELEMETRY_H

#define TELEMETRY_TAG "TELEMETRY"

#define VESC_UART_NUM UART_NUM_2
#define GPS_UART_NUM UART_NUM_1
#define GPS_BUFFER_SIZE 1024

extern TaskHandle_t telemetryUartComm;

void initUart();
void rawReadings(char* buf);
void parseNMEA(const char *subfield, float *attribute, int index);
void readElevationAndSpeed(const char *buf);
void fetchValues();

#endif