// Tạo đối tượng tinymce giả lập
window.tinymce = {
  editors: {}, // Lưu trữ các trình soạn thảo giả lập

  // Hàm khởi tạo trình soạn thảo
  init: function (settings) {
    const selector = settings.selector;
    // Tìm các phần tử textarea dựa trên selector
    const textareas = document.querySelectorAll(selector);

    // Xử lý mỗi textarea
    textareas.forEach((textarea) => {
      const editorId = textarea.id;
      if (!editorId) {
        console.warn("Textarea needs an ID for TinyMCE stub:", textarea);
        return; // Bỏ qua nếu không có ID
      }

      // Kiểm tra xem trình soạn thảo đã được khởi tạo chưa
      if (this.editors[editorId]) {
        console.log(`TinyMCE stub already initialized for #${editorId}`);
        // Cập nhật lại nội dung nếu cần
        this.editors[editorId].setContent(textarea.value);
        return;
      }

      // Ẩn textarea gốc
      textarea.style.display = "none";

      // Tạo container cho trình soạn thảo
      const editorContainer = document.createElement("div");
      editorContainer.className = "tiny-stub-editor tiny-stub-" + editorId;
      editorContainer.style.border = "1px solid #ccc";
      editorContainer.style.borderRadius = "4px";
      editorContainer.style.padding = "10px";
      editorContainer.style.backgroundColor = "#fff";
      editorContainer.style.minHeight = (settings.height || "400") + "px";
      editorContainer.style.marginBottom = "15px";
      editorContainer.style.transition = "border-color 0.2s ease-in-out";

      // Tạo thanh công cụ đơn giản
      const toolbar = document.createElement("div");
      toolbar.className = "tiny-stub-toolbar";
      toolbar.style.marginBottom = "10px";
      toolbar.style.padding = "5px";
      toolbar.style.borderBottom = "1px solid #eee";

      // Thêm nút Bold
      const boldBtn = document.createElement("button");
      boldBtn.innerHTML = "<strong>B</strong>";
      boldBtn.className = "btn btn-sm btn-outline-secondary me-1";
      boldBtn.title = "Bold";
      boldBtn.type = "button";
      toolbar.appendChild(boldBtn);

      // Thêm nút Italic
      const italicBtn = document.createElement("button");
      italicBtn.innerHTML = "<em>I</em>";
      italicBtn.className = "btn btn-sm btn-outline-secondary me-1";
      italicBtn.title = "Italic";
      italicBtn.type = "button";
      toolbar.appendChild(italicBtn);

      // Thêm nút Underline
      const underlineBtn = document.createElement("button");
      underlineBtn.innerHTML = "<u>U</u>";
      underlineBtn.className = "btn btn-sm btn-outline-secondary me-1";
      underlineBtn.title = "Underline";
      underlineBtn.type = "button";
      toolbar.appendChild(underlineBtn);

      // Thêm nút List số
      const numberListBtn = document.createElement("button");
      numberListBtn.innerHTML = '<i class="bi bi-list-ol"></i>';
      numberListBtn.className = "btn btn-sm btn-outline-secondary me-1";
      numberListBtn.title = "Numbered List";
      numberListBtn.type = "button";
      toolbar.appendChild(numberListBtn);

      // Thêm nút List chấm
      const bulletListBtn = document.createElement("button");
      bulletListBtn.innerHTML = '<i class="bi bi-list-ul"></i>';
      bulletListBtn.className = "btn btn-sm btn-outline-secondary me-1";
      bulletListBtn.title = "Bullet List";
      bulletListBtn.type = "button";
      toolbar.appendChild(bulletListBtn);

      // Thêm nút Tab
      const tabBtn = document.createElement("button");
      tabBtn.innerHTML = '<i class="bi bi-arrow-right"></i>';
      tabBtn.className = "btn btn-sm btn-outline-secondary me-1";
      tabBtn.title = "Tab";
      tabBtn.type = "button";
      toolbar.appendChild(tabBtn);

      // Thêm select box cho font size
      const fontSizeSelect = document.createElement("select");
      fontSizeSelect.className =
        "form-select form-select-sm d-inline-block w-auto me-1";
      fontSizeSelect.style.width = "auto";
      fontSizeSelect.innerHTML = `
        <option value="1">Cỡ chữ</option>
        <option value="1">12px</option>
        <option value="2">14px</option>
        <option value="3" selected>16px</option>
        <option value="4">18px</option>
        <option value="5">20px</option>
        <option value="6">24px</option>
        <option value="7">28px</option>
      `;
      toolbar.appendChild(fontSizeSelect);

      // Khu vực soạn thảo
      const editorArea = document.createElement("div");
      editorArea.className = "tiny-stub-content";
      editorArea.style.minHeight = (settings.height || 400) - 60 + "px"; // Điều chỉnh chiều cao
      editorArea.contentEditable = true;
      editorArea.style.padding = "10px";
      editorArea.style.outline = "none";
      editorArea.style.transition = "box-shadow 0.2s ease-in-out";
      editorArea.innerHTML = textarea.value; // Lấy nội dung ban đầu từ textarea

      // Thêm style cho hover và focus
      editorArea.addEventListener("mouseenter", function () {
        editorContainer.style.borderColor = "#4a8f7b";
      });

      editorArea.addEventListener("mouseleave", function () {
        if (document.activeElement !== editorArea) {
          editorContainer.style.borderColor = "#ccc";
        }
      });

      editorArea.addEventListener("focus", function () {
        editorContainer.style.borderColor = "#4a8f7b";
      });

      editorArea.addEventListener("blur", function () {
        editorContainer.style.borderColor = "#ccc";
      });

      // Ghép các thành phần
      editorContainer.appendChild(toolbar);
      editorContainer.appendChild(editorArea);

      // Chèn trình soạn thảo vào sau textarea
      textarea.parentNode.insertBefore(editorContainer, textarea.nextSibling);

      // Cập nhật nội dung textarea khi soạn thảo
      editorArea.addEventListener("input", function () {
        textarea.value = this.innerHTML;
      });

      // Xử lý các nút định dạng
      boldBtn.addEventListener("click", function () {
        document.execCommand("bold", false, null);
        editorArea.focus();
      });

      italicBtn.addEventListener("click", function () {
        document.execCommand("italic", false, null);
        editorArea.focus();
      });

      underlineBtn.addEventListener("click", function () {
        document.execCommand("underline", false, null);
        editorArea.focus();
      });

      // Xử lý nút list số
      numberListBtn.addEventListener("click", function () {
        document.execCommand("insertOrderedList", false, null);
        editorArea.focus();
      });

      // Xử lý nút list chấm
      bulletListBtn.addEventListener("click", function () {
        document.execCommand("insertUnorderedList", false, null);
        editorArea.focus();
      });

      // Xử lý nút tab
      tabBtn.addEventListener("click", function () {
        const selection = window.getSelection();
        const range = selection.getRangeAt(0);
        const tabNode = document.createTextNode("\u00A0\u00A0\u00A0\u00A0");
        range.insertNode(tabNode);
        range.setStartAfter(tabNode);
        range.setEndAfter(tabNode);
        selection.removeAllRanges();
        selection.addRange(range);
        editorArea.focus();
      });

      // Xử lý thay đổi font size
      fontSizeSelect.addEventListener("change", function () {
        const size = this.value;
        document.execCommand("fontSize", false, size);
        editorArea.focus();
      });

      // Lưu đối tượng editor giả lập
      this.editors[editorId] = {
        id: editorId,
        textarea: textarea,
        editorContainer: editorContainer,
        editorArea: editorArea,
        settings: settings,
        save: function () {
          this.textarea.value = this.editorArea.innerHTML;
        },
        getContent: function () {
          return this.editorArea.innerHTML;
        },
        setContent: function (content) {
          this.editorArea.innerHTML = content;
          this.textarea.value = content; // Đồng bộ với textarea
        },
        remove: function () {
          // Xóa container khỏi DOM và xóa khỏi danh sách trình soạn thảo
          if (this.editorContainer.parentNode) {
            this.editorContainer.parentNode.removeChild(this.editorContainer);
          }
          this.textarea.style.display = ""; // Hiển thị lại textarea gốc
          delete window.tinymce.editors[this.id];
          console.log(`TinyMCE stub removed for #${this.id}`);
        },
      };
      console.log(`TinyMCE stub initialized for #${editorId}`);
    });

    // Trả về mảng các trình soạn thảo đã khởi tạo (nếu cần)
    // return textareas.map(ta => this.editors[ta.id]).filter(Boolean);
  },

  // Lấy trình soạn thảo theo ID
  get: function (id) {
    return this.editors[id] || null; // Trả về trình soạn thảo hoặc null nếu không tìm thấy
  },

  // Hàm save toàn cục (có thể không cần thiết nếu dùng save() của từng trình soạn thảo)
  /*
  save: function() {
    for (const id in this.editors) {
      this.editors[id].save();
    }
  }
  */

  // Biến activeEditor giả lập (có thể không cần nếu dùng get(id))
  activeEditor: {
    save: function () {
      console.warn(
        "tinymce.activeEditor.save() is deprecated in stub. Use tinymce.get(id).save() instead."
      );
    },
    getContent: function () {
      console.warn(
        "tinymce.activeEditor.getContent() is deprecated in stub. Use tinymce.get(id).getContent() instead."
      );
      return "";
    },
    setContent: function (content) {
      console.warn(
        "tinymce.activeEditor.setContent() is deprecated in stub. Use tinymce.get(id).setContent() instead."
      );
    },
  },
};
