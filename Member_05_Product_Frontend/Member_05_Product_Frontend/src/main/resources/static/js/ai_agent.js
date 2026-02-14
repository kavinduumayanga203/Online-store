(function(){
  function appendMessage(container, text, cls){
    const p = document.createElement('div'); p.textContent = text; p.className = cls; p.style.margin='6px 0'; container.appendChild(p); container.scrollTop = container.scrollHeight;
  }

  async function sendToBackend(prompt){
    try{
      const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
      const body = { prompt };
      const headers = { 'Content-Type': 'application/json' };
      if(token && header) headers[header] = token;

      const resp = await fetch('/ai/query', { method: 'POST', headers, body: JSON.stringify(body) });
      if(!resp.ok){
        const err = await resp.json().catch(()=>({reply:resp.statusText}));
        return err.reply || 'AI service returned error';
      }
      const json = await resp.json();
      return json.reply || JSON.stringify(json);
    }catch(e){
      return 'AI request failed: ' + e.message;
    }
  }

  document.addEventListener('DOMContentLoaded', function(){
    const openBtn = document.getElementById('aiOpen');
    const closeBtn = document.getElementById('aiClose');
    const assistant = document.getElementById('aiAssistant');
    const body = document.getElementById('aiAssistantBody');
    const input = document.getElementById('aiInput');
    const send = document.getElementById('aiSend');
    if(openBtn) openBtn.addEventListener('click', ()=> assistant.style.display = 'flex');
    if(closeBtn) closeBtn.addEventListener('click', ()=> assistant.style.display = 'none');
    if(send){send.addEventListener('click', async ()=>{
      const v = input.value && input.value.trim(); if(!v) return; appendMessage(body, 'You: ' + v, 'text-muted'); input.value='';
      appendMessage(body, 'AI: (thinking...)', 'text-info');
      const reply = await sendToBackend(v);
      appendMessage(body, reply, 'font-weight-bold');
    });}
  });
})();
