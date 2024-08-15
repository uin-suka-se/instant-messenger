import 'zone.js/node';

import { ngExpressEngine } from '@nguniversal/express-engine';
import { APP_BASE_HREF } from '@angular/common';
import express from 'express';
import { join } from 'path';
import AppServerModule from './src/main.server';

const app = express();

const DIST_FOLDER = join(process.cwd(), 'dist/chat-messenger/browser');

app.engine('html', ngExpressEngine({
  bootstrap: AppServerModule,
}));

app.set('view engine', 'html');
app.set('views', DIST_FOLDER);

app.get('*.*', express.static(DIST_FOLDER, {
  maxAge: '1y'
}));

app.get('*', (req, res) => {
  res.render('index', { req, providers: [{ provide: APP_BASE_HREF, useValue: req.baseUrl }] });
});

const PORT = 4200;
app.listen(PORT, () => {
  console.log(`Node Express server listening on http://localhost:${PORT}`);
});
