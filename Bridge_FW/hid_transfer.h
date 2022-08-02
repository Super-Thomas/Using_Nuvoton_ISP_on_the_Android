/**************************************************************************//**
 * @file     usbd_hid.h
 * @brief    NANO100 series USB driver header file
 * @version  2.0.0
 * @date     22, March, 2013
 *
 * @note
 * Copyright (C) 2013 Nuvoton Technology Corp. All rights reserved.
 ******************************************************************************/
#ifndef __USBD_HID_H__
#define __USBD_HID_H__

/* Define the vendor id and product id */
#define USBD_VID        0x0416
#define USBD_PID        0x5020

/*!<Define HID Class Specific Request */
#define GET_REPORT          0x01
#define GET_IDLE            0x02
#define GET_PROTOCOL        0x03
#define SET_REPORT          0x09
#define SET_IDLE            0x0A
#define SET_PROTOCOL        0x0B

/*!<USB HID Interface Class protocol */
#define HID_NONE            0x00
#define HID_KEYBOARD        0x01
#define HID_MOUSE           0x02

/*!<USB HID Class Report Type */
#define HID_RPT_TYPE_INPUT      0x01
#define HID_RPT_TYPE_OUTPUT     0x02
#define HID_RPT_TYPE_FEATURE    0x03

/*-------------------------------------------------------------*/
/* Define EP maximum packet size */
#define EP0_MAX_PKT_SIZE    8
#define EP1_MAX_PKT_SIZE    EP0_MAX_PKT_SIZE
#define EP2_MAX_PKT_SIZE    64
#define EP3_MAX_PKT_SIZE    64

#define SETUP_BUF_BASE  0
#define SETUP_BUF_LEN   8
#define EP0_BUF_BASE    (SETUP_BUF_BASE + SETUP_BUF_LEN)
#define EP0_BUF_LEN     EP0_MAX_PKT_SIZE
#define EP1_BUF_BASE    (SETUP_BUF_BASE + SETUP_BUF_LEN)
#define EP1_BUF_LEN     EP1_MAX_PKT_SIZE
#define EP2_BUF_BASE    (EP1_BUF_BASE + EP1_BUF_LEN)
#define EP2_BUF_LEN     EP2_MAX_PKT_SIZE
#define EP3_BUF_BASE    (EP2_BUF_BASE + EP2_BUF_LEN)
#define EP3_BUF_LEN     EP3_MAX_PKT_SIZE

/* Define the EP number */
#define INT_IN_EP_NUM       0x01
#define INT_OUT_EP_NUM      0x02

/* Define Descriptor information */
#define HID_DEFAULT_INT_IN_INTERVAL     1
#define USBD_SELF_POWERED               0
#define USBD_REMOTE_WAKEUP              0
#define USBD_MAX_POWER                  50  /* The unit is in 2mA. ex: 50 * 2mA = 100mA */

#define LEN_CONFIG_AND_SUBORDINATE      (LEN_CONFIG+LEN_INTERFACE+LEN_HID+LEN_ENDPOINT)

/*-------------------------------------------------------------*/

/*-------------------------------------------------------------*/
void HID_Init(void);
void HID_ClassRequest(void);

void EP2_Handler(void);
void EP3_Handler(void);
void HID_GetOutReport(uint8_t *pu8EpBuf, uint32_t u32Size);
void HID_SendData(uint8_t* pu8Buffer, uint16_t u16Length);

//
#define CMD_UPDATE_APROM            0x000000A0
#define CMD_UPDATE_CONFIG           0x000000A1
#define CMD_READ_CONFIG             0x000000A2
#define CMD_ERASE_ALL               0x000000A3
#define CMD_SYNC_PACKNO             0x000000A4
#define CMD_GET_FWVER               0x000000A6
#define CMD_SET_APPINFO             0x000000A7
#define CMD_GET_APPINFO             0x000000A8
#define CMD_RUN_APROM               0x000000AB
#define CMD_RUN_LDROM               0x000000AC
#define CMD_RESET                   0x000000AD
#define CMD_CONNECT                 0x000000AE
#define CMD_DISCONNECT              0x000000AF

#define CMD_GET_DEVICEID            0x000000B1

#define CMD_UPDATE_DATAFLASH        0x000000C3
#define CMD_WRITE_CHECKSUM          0x000000C9
#define CMD_GET_FLASHMODE           0x000000CA

#define CMD_RESEND_PACKET           0x000000FF

/*-------------------------------------------------------------*/
/* Define maximum packet size */
#define MAX_PKT_SIZE	64

void UART1_Init(void);
void UART1_SendData(uint8_t* pu8Buffer, uint16_t u16Length);
void Bridge_Process(void);

#endif  /* __USBD_HID_H_ */

/*** (C) COPYRIGHT 2013 Nuvoton Technology Corp. ***/
