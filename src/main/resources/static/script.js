// 获取按钮和进度条元素
const startButton = document.getElementById('start-button');
const progressBar = document.getElementById('progress-bar');

// 点击按钮时触发的事件
startButton.addEventListener('click', async () => {
    try {
        // 禁用按钮，防止重复点击
        startButton.disabled = true;

        // 模拟进度条从 0% 到 100%
        let progress = 0;
        const interval = setInterval(() => {
            if (progress < 100) {
                progress++;
                progressBar.style.width = progress + '%';
                progressBar.textContent = progress + '%';
            } else {
                clearInterval(interval);
            }
        }, 100);

        // 调用后端接口
        const response = await fetch('/automation/start');
        if (response.ok) {
            console.log('服务启动成功');
        } else {
            console.error('服务启动失败');
        }

        // 启用按钮
        startButton.disabled = false;
    } catch (error) {
        console.error('请求出错:', error);
        startButton.disabled = false;
    }
});