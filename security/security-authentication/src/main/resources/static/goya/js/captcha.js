/**
 * 滑动验证码组件
 */
class JigsawCaptcha {
    constructor(containerId, options) {
        this.containerId = containerId;
        this.container = $('#' + containerId);
        this.contextPath = options.contextPath || '';
        this.sessionId = options.sessionId || '';
        this.category = options.category || 'JIGSAW';
        this.onVerify = options.onVerify || function() {};
        
        this.originalImageBase64 = '';
        this.sliderImageBase64 = '';
        this.identity = '';
        this.isVerified = false;
        this.coordinate = null;
        
        this.init();
    }
    
    init() {
        this.render();
    }
    
    render() {
        let html = `
            <div class="jigsaw-captcha">
                <div class="jigsaw-captcha-bg">
                    <img class="jigsaw-original-image" src="" alt="验证码背景">
                    <img class="jigsaw-slider-image" src="" alt="滑块">
                </div>
                <div class="jigsaw-captcha-tip">
                    <span class="jigsaw-tip-text">请拖动滑块完成验证</span>
                    <button type="button" class="jigsaw-refresh-btn">刷新</button>
                </div>
            </div>
        `;
        this.container.html(html);
        
        // 绑定刷新按钮
        this.container.find('.jigsaw-refresh-btn').on('click', () => {
            this.refresh();
        });
        
        // 绑定滑块拖拽事件
        this.bindDragEvents();
    }
    
    load() {
        let url = this.contextPath + '/open/captcha';
        let params = {
            identity: this.sessionId,
            category: this.category
        };
        
        $.http.get(url, params)
            .then(result => {
                // 支持两种响应格式：result.success 或 result.isSuccess
                if ((result.success || result.isSuccess) && result.data) {
                    this.originalImageBase64 = result.data.originalImageBase64;
                    this.sliderImageBase64 = result.data.sliderImageBase64;
                    this.identity = result.data.identity;
                    
                    // 处理base64格式：如果已经是完整的data URI，直接使用；否则添加前缀
                    let originalSrc = this.originalImageBase64;
                    if (!originalSrc.startsWith('data:')) {
                        originalSrc = 'data:image/png;base64,' + originalSrc;
                    }
                    
                    let sliderSrc = this.sliderImageBase64;
                    if (!sliderSrc.startsWith('data:')) {
                        sliderSrc = 'data:image/png;base64,' + sliderSrc;
                    }
                    
                    this.container.find('.jigsaw-original-image').attr('src', originalSrc);
                    this.container.find('.jigsaw-slider-image').attr('src', sliderSrc);
                    
                    // 等待图片加载完成后重置
                    let originalImg = this.container.find('.jigsaw-original-image')[0];
                    let sliderImg = this.container.find('.jigsaw-slider-image')[0];
                    
                    if (originalImg.complete && sliderImg.complete) {
                        this.reset();
                    } else {
                        let loadedCount = 0;
                        let self = this;
                        originalImg.onload = function() {
                            loadedCount++;
                            if (loadedCount === 2) {
                                self.reset();
                            }
                        };
                        sliderImg.onload = function() {
                            loadedCount++;
                            if (loadedCount === 2) {
                                self.reset();
                            }
                        };
                    }
                } else {
                    this.showError('加载验证码失败');
                }
            })
            .catch(error => {
                this.showError('加载验证码失败');
            });
    }
    
    refresh() {
        this.reset();
        this.load();
    }
    
    reset() {
        this.isVerified = false;
        this.coordinate = null;
        let sliderImage = this.container.find('.jigsaw-slider-image');
        sliderImage.css('left', '0px');
        this.container.find('.jigsaw-tip-text').text('请拖动滑块完成验证').removeClass('success error');
        this.container.find('.jigsaw-captcha-bg').removeClass('success error');
    }
    
    bindDragEvents() {
        let self = this;
        let sliderImage = this.container.find('.jigsaw-slider-image');
        let bgImage = this.container.find('.jigsaw-original-image');
        let bgContainer = this.container.find('.jigsaw-captcha-bg');
        let isDragging = false;
        let startX = 0;
        let startLeft = 0;
        let maxLeft = 0;
        let bgImageWidth = 0;
        let sliderImageWidth = 0;
        
        // 计算最大拖动距离
        function calculateDimensions() {
            // 获取背景图的实际显示宽度和高度
            bgImageWidth = bgImage.width();
            let bgImageHeight = bgImage.height();
            
            // 获取滑块图片的原始尺寸
            let sliderNaturalWidth = 0;
            let sliderNaturalHeight = 0;
            
            if (sliderImage[0].complete && sliderImage[0].naturalWidth > 0) {
                sliderNaturalWidth = sliderImage[0].naturalWidth;
                sliderNaturalHeight = sliderImage[0].naturalHeight;
            } else {
                // 如果图片还没加载完成，等待加载
                return;
            }
            
            // 滑块图片高度应该等于背景图高度（因为要匹配缺失区域）
            // 计算缩放比例
            let scale = bgImageHeight / sliderNaturalHeight;
            sliderImageWidth = sliderNaturalWidth * scale;
            
            // 设置滑块图片的显示尺寸
            sliderImage.css({
                'height': bgImageHeight + 'px',
                'width': sliderImageWidth + 'px'
            });
            
            // 最大拖动距离 = 背景图宽度 - 滑块图片宽度
            maxLeft = bgImageWidth - sliderImageWidth;
            if (maxLeft < 0) maxLeft = 0;
        }
        
        // 图片加载完成后计算
        bgImage.on('load', function() {
            calculateDimensions();
        });
        
        sliderImage.on('load', function() {
            calculateDimensions();
        });
        
        // 初始化时计算
        setTimeout(function() {
            calculateDimensions();
        }, 300);
        
        // 窗口大小改变时重新计算
        $(window).on('resize', function() {
            calculateDimensions();
        });
        
        // 鼠标按下
        sliderImage.on('mousedown touchstart', function(e) {
            if (self.isVerified) return false;
            isDragging = true;
            calculateDimensions();
            startX = (e.clientX || (e.touches && e.touches[0].clientX)) || 0;
            startLeft = parseInt(sliderImage.css('left')) || 0;
            bgContainer.addClass('dragging');
            e.preventDefault();
            return false;
        });
        
        // 鼠标移动
        $(document).on('mousemove touchmove', function(e) {
            if (!isDragging || self.isVerified) return;
            
            let currentX = (e.clientX || (e.touches && e.touches[0].clientX)) || 0;
            if (!currentX) return;
            
            // 计算相对于背景容器的偏移
            let bgOffset = bgContainer.offset();
            let relativeX = currentX - bgOffset.left;
            
            // 滑块图片的左边位置
            let newLeft = relativeX - sliderImageWidth / 2;
            
            // 限制拖动范围：最左到背景图右边界减去滑块宽度
            if (newLeft < 0) newLeft = 0;
            if (newLeft > maxLeft) newLeft = maxLeft;
            
            sliderImage.css('left', newLeft + 'px');
            e.preventDefault();
        });
        
        // 鼠标释放
        $(document).on('mouseup touchend', function(e) {
            if (!isDragging || self.isVerified) return;
            
            isDragging = false;
            bgContainer.removeClass('dragging');
            
            let finalLeft = parseInt(sliderImage.css('left')) || 0;
            
            // 重新获取实际尺寸
            calculateDimensions();
            
            // 计算x坐标：需要将显示尺寸转换为原始图片尺寸
            // 背景图可能被缩放显示，需要计算缩放比例
            let bgNaturalWidth = 0;
            let bgNaturalHeight = 0;
            
            if (bgImage[0].complete && bgImage[0].naturalWidth > 0) {
                bgNaturalWidth = bgImage[0].naturalWidth;
                bgNaturalHeight = bgImage[0].naturalHeight;
            } else {
                // 如果图片还没加载完成，使用显示尺寸
                bgNaturalWidth = bgImageWidth;
            }
            
            // 计算缩放比例：原始宽度 / 显示宽度
            let scaleX = bgNaturalWidth / bgImageWidth;
            
            // 将显示尺寸的left转换为原始图片尺寸的x坐标
            let xCoordinate = Math.round(finalLeft * scaleX);
            
            // 确保坐标在有效范围内
            if (xCoordinate < 0) xCoordinate = 0;
            if (xCoordinate > bgNaturalWidth) xCoordinate = bgNaturalWidth;
            
            // 验证坐标
            self.verify(xCoordinate);
            
            e.preventDefault();
        });
    }
    
    verify(x) {
        let coordinate = {
            x: x,
            y: 5  // 固定为5，对应后端的BOLD常量
        };
        
        let url = this.contextPath + '/open/captcha';
        let verificationData = {
            identity: this.identity,
            category: {code: this.category},
            coordinate: coordinate
        };
        
        $.http.post(url, verificationData, 'json')
            .then(result => {
                // 支持两种响应格式：result.success 或 result.isSuccess
                if (result.success || result.isSuccess) {
                    this.isVerified = true;
                    this.coordinate = coordinate;
                    this.container.find('.jigsaw-tip-text').text('验证成功').addClass('success');
                    this.container.find('.jigsaw-captcha-bg').addClass('success');
                    // 延迟调用回调，确保UI更新完成
                    setTimeout(() => {
                        this.onVerify(coordinate);
                    }, 100);
                } else {
                    this.showError('验证失败，请重试');
                    this.refresh();
                }
            })
            .catch(error => {
                this.showError('验证失败，请重试');
                this.refresh();
            });
    }
    
    showError(message) {
        this.container.find('.jigsaw-tip-text').text(message).addClass('error');
        this.container.find('.jigsaw-slider-track').addClass('error');
    }
    
    getCoordinate() {
        return this.coordinate;
    }
    
    getVerified() {
        return this.isVerified;
    }
}

