#ifndef TELEMETRY_H
#define TELEMETRY_H

#define TELEMETRY_TAG "TELEMETRY"

#define GPS_UART_NUM UART_NUM_2

void initUart();
void readElevation();
void fetchVesc();

#endif