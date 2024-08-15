import {
  Component,
  EventEmitter,
  Inject,
  Input,
  OnChanges,
  OnInit,
  Output,
  PLATFORM_ID,
  SimpleChanges,
} from '@angular/core';
import { ChatService } from '../../services/chat.service';
import * as _ from 'lodash';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-chat-card',
  templateUrl: './chat-card.component.html',
  styleUrl: './chat-card.component.scss',
})
export class ChatCardComponent implements OnInit, OnChanges {
  @Input('id') chatId: string;
  @Input('type') chatType: string;
  @Input('name') chatName: string;
  @Input('sentAt') chatDate: string;
  @Input('nim') senderId: string;
  @Input('lastMessage') lastMessage: string;
  @Input('chatDetail') chatDetail: string;
  @Output() openChat = new EventEmitter();
  selectedChatId: string;
  messages: chatMessage[];
  changeData: boolean;

  constructor(
    private _chatService: ChatService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    this.loadChatDetail(this.chatId);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chatDate']) {
      this.changeData = true;
    }
  }

  ngAfterViewChecked(): void {
    if (this.changeData && isPlatformBrowser(this.platformId) && this.chatDate) {
      this.chatDate = this.formatSentAt(this.chatDate);
    }
    this.changeData = false;
  }

  loadChatDetail(chatId: string): void {
    this.selectedChatId = chatId;
    this._chatService
      .getChatDetail(chatId, this.senderId)
      .subscribe((data: any) => {
        this.chatDetail = _.cloneDeep(data);
        this.messages = data.messages;
      });
  }

  formatSentAt(sentAt: string): string {
    const daysOfWeek = [
      'Minggu',
      'Senin',
      'Selasa',
      'Rabu',
      'Kamis',
      'Jumat',
      'Sabtu',
    ];
    const sentDate = new Date(sentAt);
    const now = new Date();

    // Helper function to check if two dates are on the same day
    const isSameDay = (d1: Date, d2: Date): boolean => {
      return (
        d1.getFullYear() === d2.getFullYear() &&
        d1.getMonth() === d2.getMonth() &&
        d1.getDate() === d2.getDate()
      );
    };

    // Helper function to get the start of the week (Sunday)
    const getStartOfWeek = (date: Date): Date => {
      const start = new Date(date);
      const day = start.getDay();
      const diff = start.getDate() - day;
      start.setDate(diff);
      start.setHours(0, 0, 0, 0); // Reset hours to start of the day
      return start;
    };

    // Check if the date is today
    if (isSameDay(sentDate, now)) {
      return sentDate.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
      });
    }

    // Check if the date is within this week
    const startOfWeek = getStartOfWeek(now);
    if (sentDate >= startOfWeek) {
      return daysOfWeek[sentDate.getDay()];
    }

    // If the date is more than a week ago, return the date
    return sentDate.toLocaleDateString();
  }

  open() {
    this.openChat.emit(this.chatDetail);
  }
}

interface chatMessage {
  attachments: string;
  chatId: string;
  content: string;
  senderId: string;
  sentAt: string;
  _id: string;
  senderName: string;
}
