import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private subject: Subject<MessageEvent>;
  private ws: WebSocket;
  private url: string;
  private reconnectInterval: number = 3000; // 3 seconds for quicker reconnection
  private heartbeatTimer: any;
  private isConnecting: boolean = false;

  constructor(@Inject(PLATFORM_ID) private platformId: any) {}

  public connect(url: string = 'ws://localhost:8181/ws'): Subject<MessageEvent> {
    this.url = url;
    if (isPlatformBrowser(this.platformId)) {
      if (!this.subject || this.subject.closed) {
        this.subject = this.create(url);
      }
      return this.subject;
    } else {
      throw new Error('WebSocket is not supported in this environment.');
    }
  }

  private create(url: string): Subject<MessageEvent> {
    if (this.isConnecting) return this.subject;
    this.isConnecting = true;
    console.log(`Attempting to connect to WebSocket at ${url}`);
    this.ws = new WebSocket(url);

    const observable = new Observable((observer: any) => {
      this.ws.onopen = () => {
        console.log('WebSocket connection opened');
        this.ws.send(JSON.stringify({ id: 'chat 1' }));
        this.isConnecting = false;
      };

      this.ws.onmessage = (event) => {
        console.log('WebSocket message received:', event);
        observer.next(event);
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        observer.error(error);
        this.isConnecting = false;
        this.reconnect();
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket connection closed:', event);
        observer.complete();
        this.isConnecting = false;
        this.stopHeartbeat();
        if (!event.wasClean) {
          this.reconnect();
        }
      };

      return () => {
        this.ws.close();
      };
    });

    const observer = {
      next: (data: Object) => {
        if (this.ws.readyState === WebSocket.OPEN) {
          console.log('Sending message via WebSocket:', data);
          this.ws.send(JSON.stringify(data));
        } else {
          console.warn('WebSocket is not open:', this.ws.readyState);
        }
      },
    };

    return Subject.create(observer, observable);
  }

  private reconnect() {
    if (this.isConnecting) return;
    console.log(`Attempting to reconnect WebSocket in ${this.reconnectInterval / 1000} seconds...`);
    setTimeout(() => {
      this.subject = this.create(this.url);
    }, this.reconnectInterval);
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
    }
  }
}
