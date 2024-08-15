import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
  AfterViewChecked,
  Inject,
  PLATFORM_ID,
  AfterViewInit,
  ChangeDetectorRef,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ChatService } from '../../services/chat.service';
import { UntypedFormControl } from '@angular/forms';

@Component({
  selector: 'app-chat-detail',
  templateUrl: './chat-detail.component.html',
  styleUrls: ['./chat-detail.component.scss'],
})
export class ChatDetailComponent
  implements OnChanges, AfterViewChecked, AfterViewInit
{
  @ViewChild('messageContainer') private messageContainer: ElementRef;
  @Input('detail') chatDetail;
  @Input('nim') senderId: string;
  @Output() closeChat = new EventEmitter();
  @Output() sendMessage = new EventEmitter();
  private shouldScroll: boolean = false;

  messageFormControl: string;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private chatService: ChatService,
    private changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chatDetail']) {
      this.shouldScroll = true;
    }
  }

  ngAfterViewInit(): void {
    this.chatService?.chatDetailListener$?.subscribe(() => {
      this.scrollToBottom();
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll && isPlatformBrowser(this.platformId)) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private scrollToBottom(): void {
    if (isPlatformBrowser(this.platformId) && this.messageContainer) {
      try {
        this.messageContainer.nativeElement.scrollIntoView({
          behavior: 'smooth',
          block: 'end',
        });
      } catch (err) {
        console.error('Error scrolling to bottom:', err);
      }
    }
  }

  onTextAreaInput(event) {
    const target = event.target as HTMLTextAreaElement;
    this.changeDetectorRef.detach();
    target.style.height = '0';
    target.style.height = target.scrollHeight + 2 + 'px';
    this.changeDetectorRef.reattach();
  }

  onKeyPress(event) {
    const target = event.target as HTMLTextAreaElement;
    let result = true;

    if (event.key === 'Enter' && !event.ctrlKey && !event.shiftKey) {
      target.value = '';
      target.click();
      result = false;
      this.send();
    } else if (event.key === '\n' && event.ctrlKey) {
      target.value = target.value + '\n';
      target.setSelectionRange(target.selectionStart, target.selectionEnd + 1);
    }

    target.style.height = '0';
    target.style.height = target.scrollHeight + 2 + 'px';

    this.changeDetectorRef.markForCheck();
    return result;
  }

  send() {
    this.sendMessage?.emit(this.messageFormControl)
  }

  formatTime(dateTime: string): string {
    const date = new Date(dateTime);

    let hours = date.getUTCHours();
    const minutes = date.getUTCMinutes();
    const ampm = hours >= 12 ? 'PM' : 'AM';

    hours = hours % 12;
    hours = hours ? hours : 12; // the hour '0' should be '12'

    const strMinutes = minutes < 10 ? '0' + minutes : minutes.toString();
    const strHours = hours < 10 ? '0' + hours : hours.toString();

    return `${strHours}:${strMinutes} ${ampm}`;
  }

  close() {
    this.closeChat.emit('');
  }
}
