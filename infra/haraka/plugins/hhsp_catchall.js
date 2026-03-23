/**
 * Haraka plugin: hhsp_catchall
 *
 * Accepts all inbound email to @customer.hhsp.dev (catch-all),
 * parses the short_id from the local part, and forwards the
 * full message payload to the Spring Boot API via HTTP POST.
 *
 * Address format:  reply-{short_id}@customer.hhsp.dev
 * Example:         reply-abc123xyz456@customer.hhsp.dev  →  short_id = "abc123xyz456"
 */

'use strict';

const SHORT_ID_RE = /^[a-z]+-([a-z0-9]+)@customer\.hhsp\.dev$/i;

// --- RCPT hook: accept all mail for customer.hhsp.dev ---
exports.hook_rcpt = function (next, connection, params) {
    const rcpt = params[0];
    if (rcpt.host.toLowerCase() === 'customer.hhsp.dev') {
        return next(OK);
    }
    return next(DENY, 'Unknown recipient');
};

// --- DATA_POST hook: parse and forward to Spring Boot ---
exports.hook_data_post = function (next, connection) {
    const txn    = connection.transaction;
    const toAddr = txn.rcpt_to && txn.rcpt_to[0] ? txn.rcpt_to[0].address() : '';
    const from   = txn.mail_from ? txn.mail_from.address() : '';

    const match   = toAddr.match(SHORT_ID_RE);
    const shortId = match ? match[1] : null;

    const subject     = txn.header.get('Subject')      || '';
    const messageId   = txn.header.get('Message-ID')   || '';
    const inReplyTo   = txn.header.get('In-Reply-To')  || '';

    // Extract text body (Haraka populates txn.body after mime parsing)
    const textBody = (txn.body && txn.body.bodytext) ? txn.body.bodytext : '';

    // Build attachments list
    const attachments = [];
    if (txn.attachment_hooks) {
        txn.attachment_hooks.forEach(function (hook) {
            attachments.push({
                filename    : hook.filename || 'attachment',
                content_type: hook.content_type || 'application/octet-stream',
                size        : hook.body ? hook.body.length : 0,
            });
        });
    }

    const payload = JSON.stringify({
        to         : toAddr,
        from       : from,
        short_id   : shortId,
        subject    : subject,
        text       : textBody,
        headers    : {
            'Message-ID'  : messageId,
            'In-Reply-To' : inReplyTo,
        },
        attachments: attachments,
    });

    const springBootUrl  = process.env.SPRING_BOOT_URL   || 'http://localhost:8080';
    const internalSecret = process.env.INTERNAL_SECRET   || '';
    const endpoint       = springBootUrl + '/v1/inbound/webhook';

    // Use built-in https/http module for zero-dependency delivery
    const urlModule = require('url');
    const parsed    = urlModule.parse(endpoint);
    const isHttps   = parsed.protocol === 'https:';
    const transport = isHttps ? require('https') : require('http');

    const options = {
        hostname : parsed.hostname,
        port     : parsed.port || (isHttps ? 443 : 80),
        path     : parsed.path,
        method   : 'POST',
        headers  : {
            'Content-Type'      : 'application/json',
            'Content-Length'    : Buffer.byteLength(payload),
            'X-Internal-Secret' : internalSecret,
        },
    };

    const req = transport.request(options, function (res) {
        connection.logdebug('[hhsp_catchall] Forwarded inbound mail to Spring Boot — status: ' + res.statusCode);
    });

    req.on('error', function (err) {
        connection.logerror('[hhsp_catchall] Failed to forward inbound mail: ' + err.message);
    });

    req.write(payload);
    req.end();

    return next(OK);
};
