import * as vscode from 'vscode';
import { vibeTelemetry } from './telemetry';

export function activate(context: vscode.ExtensionContext) {
    console.log('CodiBase extension activated');
    
    // Register commands
    context.subscriptions.push(
        vscode.commands.registerCommand('codibase.cloneRepo', cloneRepository),
        vscode.commands.registerCommand('codibase.createPR', createPullRequest),
        vscode.commands.registerCommand('codibase.aiReview', requestAiReview),
        vscode.commands.registerCommand('codibase.smartMerge', smartMergeConflicts)
    );
    
    // Start real-time collaboration
    startCollaboration(context);
    
    // Track activation
    vibeTelemetry.sendEvent('extension_activated', {
        extension: 'codibase'
    });
}

async function cloneRepository() {
    const config = vscode.workspace.getConfiguration('codibase');
    const serverUrl = config.get<string>('serverUrl');
    
    const repoUrl = await vscode.window.showInputBox({
        prompt: 'Enter CodiBase repository URL',
        placeHolder: 'https://codibase.dev/username/repo'
    });
    
    if (repoUrl) {
        const terminal = vscode.window.createTerminal('CodiBase Clone');
        terminal.show();
        terminal.sendText(`git clone ${repoUrl}`);
        
        vibeTelemetry.sendEvent('repo_cloned', { source: 'codibase' });
    }
}

async function createPullRequest() {
    const config = vscode.workspace.getConfiguration('codibase');
    const serverUrl = config.get<string>('serverUrl');
    const apiToken = config.get<string>('apiToken');
    
    // Get current branch and changes
    const title = await vscode.window.showInputBox({
        prompt: 'Pull Request Title'
    });
    
    if (title) {
        // Create PR via API
        const response = await fetch(`${serverUrl}/api/v1/pullrequests`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${apiToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                title,
                source: 'current-branch',
                target: 'main'
            })
        });
        
        if (response.ok) {
            vscode.window.showInformationMessage('Pull Request created!');
            vibeTelemetry.sendEvent('pr_created', { source: 'vibecoda' });
        }
    }
}

async function requestAiReview() {
    const editor = vscode.window.activeTextEditor;
    if (!editor) return;
    
    const selection = editor.selection;
    const code = editor.document.getText(selection);
    
    const config = vscode.workspace.getConfiguration('codibase');
    const serverUrl = config.get<string>('serverUrl');
    
    vscode.window.withProgress({
        location: vscode.ProgressLocation.Notification,
        title: 'AI reviewing your code...',
        cancellable: false
    }, async (progress) => {
        const response = await fetch(`${serverUrl}/api/v1/ai/review`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code })
        });
        
        const review = await response.json();
        
        // Show review in panel
        const panel = vscode.window.createWebviewPanel(
            'aiReview',
            'AI Code Review',
            vscode.ViewColumn.Two,
            {}
        );
        
        panel.webview.html = formatReview(review);
        
        vibeTelemetry.trackEditorAction('ai_review_requested');
    });
}

async function smartMergeConflicts() {
    // Detect merge conflicts and use AI to resolve
    const editor = vscode.window.activeTextEditor;
    if (!editor) return;
    
    const document = editor.document;
    const text = document.getText();
    
    if (text.includes('<<<<<<<')) {
        const config = vscode.workspace.getConfiguration('codibase');
        const serverUrl = config.get<string>('serverUrl');
        
        const response = await fetch(`${serverUrl}/api/v1/ai/merge`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ conflicts: text })
        });
        
        const resolved = await response.json();
        
        // Apply resolution
        const edit = new vscode.WorkspaceEdit();
        edit.replace(
            document.uri,
            new vscode.Range(0, 0, document.lineCount, 0),
            resolved.mergedCode
        );
        
        await vscode.workspace.applyEdit(edit);
        vscode.window.showInformationMessage('Conflicts resolved by AI!');
        
        vibeTelemetry.sendEvent('smart_merge_used');
    }
}

function startCollaboration(context: vscode.ExtensionContext) {
    // WebSocket connection for real-time collaboration
    const config = vscode.workspace.getConfiguration('codibase');
    const serverUrl = config.get<string>('serverUrl')?.replace('http', 'ws');
    
    const ws = new WebSocket(`${serverUrl}/ws/collaborate`);
    
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        // Handle remote edits
        handleRemoteEdit(data);
    };
    
    // Send local edits
    vscode.workspace.onDidChangeTextDocument((event) => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({
                fileId: event.document.uri.toString(),
                changes: event.contentChanges
            }));
        }
    });
}

function handleRemoteEdit(data: any) {
    // Apply remote user's edits to local file
}

function formatReview(review: any): string {
    return `
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: sans-serif; padding: 20px; }
                .suggestion { background: #fff3cd; padding: 10px; margin: 10px 0; border-radius: 5px; }
                .severity-high { border-left: 4px solid #dc3545; }
                .severity-medium { border-left: 4px solid #ffc107; }
                .severity-low { border-left: 4px solid #28a745; }
            </style>
        </head>
        <body>
            <h2>AI Code Review</h2>
            <div class="suggestion severity-${review.severity}">
                <p>${review.summary}</p>
            </div>
        </body>
        </html>
    `;
}

export function deactivate() {}
