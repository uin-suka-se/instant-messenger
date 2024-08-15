import { Component, ElementRef, OnInit, ViewChild, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ChatService } from '../services/chat.service';
import { WebSocketService } from '../services/websocket.service';
import * as _ from 'lodash';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss'],
})
export class ChatComponent implements OnInit {
  @ViewChild('appChatDetail') appChatDetail: ElementRef | undefined;
  chatList: any[] = [];
  messages: any[] = [];
  participants: any[] = [];
  selectedChatId: string;
  messageContent: string;
  selectedChatDetail;
  userData;
  listOfChatDetail;

  constructor(
    private chatService: ChatService,
    private wsService: WebSocketService,
    @Inject(PLATFORM_ID) private platformId: any
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const credentials = localStorage.getItem('credentials');
      if (credentials) {
        this.userData = JSON.parse(credentials);
        this.connectWebSocket();
        this.loadChatList();
      } else {
        console.error('No user data found in localStorage.');
        // Handle the case where credentials are not found
        // For example, redirect to login page or show an error message
      }
    }
  }

  loadChatList(): void {
    if (!this.userData) {
      console.error('No user data found.');
      return;
    }
    this.chatService.getChatList(this.userData?.nim).subscribe(
      (data: any) => {
        this.chatList = this.sortChatsByLatestMessage(data?.chats);
        this.chatList?.forEach(chat => {
          this.listOfChatDetail = [];
          this.loadChatDetail(chat?.chatId)
        })
      },
      (error: any) => {
        console.error('Failed to load chat list:', error);
      }
    );
  }

  loadChatDetail(chatId: string): void {
    this.selectedChatId = chatId;
    this.chatService.getChatDetail(chatId, this.userData?.nim).subscribe((data: any) => {
      this.listOfChatDetail.push(_.cloneDeep(data));
      this.messages = data.messages;
    });
  }

  generateUniqueId(): string {
    const array = new Uint8Array(12);
    window.crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
  }


  sendMessage(event): void {
    console.log('masuk');

    const today = new Date();
    const message = {
      content: event,
      chatId: this.selectedChatDetail?.chatId,
      sentAt: today?.toISOString(),
      senderId: this.userData?.nim,
      _id: this.generateUniqueId(),
      senderName: this.userData?.name
    };
    this.chatService.sendMessage(message).subscribe(
      (data: any) => {
        this.messages.push(data);
      },
      (error: any) => {
        console.error('Failed to send message:', error);
      }
    );
  }

  connectWebSocket(): void {
    this.wsService.connect().subscribe({
      next: (message: MessageEvent) => {
        let data;
        try {
          data = JSON.parse(message?.data);
        } catch (error) {
        }

        if (data) {
          const changedChatIndex = this.chatList?.findIndex(chat => chat.chatId === data.chatId);
          this.chatList[changedChatIndex].lastMessage = data?.content;
          this.chatList[changedChatIndex].lastMessageTime = data?.sentAt;
          this.chatList[changedChatIndex].name = data?.senderName;

          this.listOfChatDetail?.[changedChatIndex]?.messages?.push(data);

          if (data?.chatId === this.selectedChatDetail?.chatId) {
            this.selectedChatDetail?.messages?.push(data);
          }
          setTimeout(() => {
            this.chatService?.chatDetailChanged();
          }, 100);
        }
      },
      error: (error: any) => {
        console.error('WebSocket error:', error);
      },
      complete: () => {
        console.log('WebSocket connection closed');
      }
    });
  }

  downloadFile(fileId: string): void {
    this.chatService.downloadFile(fileId).subscribe(
      (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'file';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      (error: any) => {
        console.error('Failed to download file:', error);
      }
    );
  }

  sortChatsByLatestMessage(chats: any[]): any[] {
    return chats.sort((a, b) => {
      const dateA = new Date(a.lastMessageTime).getTime();
      const dateB = new Date(b.lastMessageTime).getTime();
      return dateB - dateA;
    });
  }

  selectChat(event: any): void {
    this.chatService?.chatDetailChanged();
    if (this.appChatDetail) {
      this.selectedChatDetail = event;
      this.appChatDetail.nativeElement.classList.toggle('translate-x-[100vw]');
    } else {
      console.error('appChatDetail is undefined.');
    }
  }

  closeChat(): void {
    if (this.appChatDetail) {
      this.appChatDetail.nativeElement.classList.toggle('translate-x-[100vw]');
    } else {
      console.error('appChatDetail is undefined.');
    }
  }
}
