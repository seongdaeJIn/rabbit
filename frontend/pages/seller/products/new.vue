<template>
    <section class="product-form">
      <h2 class="title">새 상품 등록</h2>
  
      <form class="form" @submit.prevent="handleSubmit">
        <label class="form-label">
            상품명 (기본 이름)
            <input v-model="form.defaultName" type="text" required />
        </label>
  
        <!-- 메인 이미지 업로드 -->
        <div class="form-label">
          메인 이미지
          <div class="upload-box" @click="triggerMainInput">
            <span v-if="!mainPreview">클릭하여 메인 이미지 선택</span>
            <img v-if="mainPreview" :src="mainPreview" alt="Main Preview" />
          </div>
          <input
            ref="mainInput"
            type="file"
            accept="image/*"
            @change="handleMainImage"
            style="display: none"
          />
        </div>
  
        <!-- 서브 이미지 업로드 -->
        <div class="form-label">
          서브 이미지 (최대 3장)
          <div class="upload-multi">
            <div
              v-for="(url, idx) in subPreviews"
              :key="idx"
              class="sub-preview"
            >
              <img :src="url" alt="Sub" />
              <button type="button" @click="removeSubImage(idx)">삭제</button>
            </div>
            <button
              type="button"
              class="add-btn"
              @click="triggerSubInput"
              :disabled="subImages.length >= 3"
            >
              + 추가
            </button>
            <input
              ref="subInput"
              type="file"
              accept="image/*"
              @change="handleSubImages"
              style="display: none"
            />
          </div>
        </div>
  
        <label class="form-label">
        제품 코드
        <input v-model="form.productCode" type="text" required />
      </label>

      <label class="form-label">
        제품 타입
        <input v-model="form.productType" type="text" required />
      </label>

      <label class="form-label">
        상품 설명
        <v-md-editor v-model="form.description" height="400px" />
      </label>

      <label class="form-label">
        대표 상품 여부 (isFeatured)
        <select v-model="form.isFeatured" required>
          <option :value="true">대표 상품</option>
          <option :value="false">일반 상품</option>
        </select>
      </label>

      <label class="form-label">
        판매 여부 (isActive)
        <select v-model="form.isActive" required>
          <option :value="true">판매중</option>
          <option :value="false">판매중지</option>
        </select>
      </label>

      <label class="form-label">
        브랜드 ID (임시)
        <input v-model="form.brandId" type="text" required />
        <!-- 실제로는 브랜드 목록을 불러와 드롭다운 처리 예정 -->
      </label>
        
        <button type="submit" class="submit-btn">등록</button>
      </form>
    </section>
  </template>
  
  <script setup lang="ts">
  definePageMeta({ layout: 'seller' })
import { fi } from '@nuxt/ui/runtime/locale/index.js'
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  
  const router = useRouter()
  const config = useRuntimeConfig()

  
  const mainImage = ref<File | null>(null)
  const mainPreview = ref<string | null>(null)
  const mainInput = ref<HTMLInputElement | null>(null)
  
  const subImages = ref<File[]>([])
  const subPreviews = ref<string[]>([])
  const subInput = ref<HTMLInputElement | null>(null)
  
  const triggerMainInput = () => {
    console.log('iii')
    if (mainInput.value) {
        mainInput.value.value = ''
        mainInput.value?.click()
    }
  }
  
  const triggerSubInput = () => {
    subInput.value?.click()
  }
  
  const handleMainImage = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) {
      mainImage.value = file
      if (mainPreview.value) URL.revokeObjectURL(mainPreview.value)
      mainPreview.value = URL.createObjectURL(file)
    }
  }
  
  const handleSubImages = (e: Event) => {
    const files = Array.from((e.target as HTMLInputElement).files || [])
    const allowed = 3 - subImages.value.length
    const selected = files.slice(0, allowed)
  
    selected.forEach((file) => {
      subImages.value.push(file)
      subPreviews.value.push(URL.createObjectURL(file))
    })
  
    if (subInput.value) subInput.value.value = ''
  }
  
  const removeSubImage = (index: number) => {
    URL.revokeObjectURL(subPreviews.value[index])
    subImages.value.splice(index, 1)
    subPreviews.value.splice(index, 1)
  }

  

const form = ref({
  defaultName: '',
  productCode: '',
  productType: '',
  description: '',
  isFeatured: false,
  isActive: true,
  brandId: null,
})



const createProduct = async (form) => {
  const res = await fetch(`${config.public.apiBase}/api/v1/products/register?`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(form),
  })

  if (!res.ok) throw new Error('상품 정보 등록 실패')

  const data = await res.json()

  return data.data; 
}

const uploadFile = async (file, productUid) => {
  const encodedName = encodeURIComponent(file.name)
  const res = await fetch(`${config.public.apiBase}/api/v1/files/upload?filename=${encodedName}&parentUid=${productUid}&parentType=PRODUCT_MAIN`, {
    method: 'POST',
    body: file,
    credentials: 'include'
  })

  if (!res.ok) throw new Error('파일 업로드 실패')

  return await res.json()
}

// 폼 전체 제출 핸들러
const handleSubmit = async () => {
  try {
    const productUid = await createProduct(form.value)

    console.log(productUid);
    const uploadRes = await uploadFile(mainImage.value, productUid)

    if (uploadRes.success) {
      alert('상품과 이미지 등록 성공')
      await router.push('/seller/products')
    } else {
      alert('이미지 등록 실패')
    }
  } catch (e) {
    console.error(e)
    alert(e.message || '네트워크 오류 발생')
  }
}
  </script>
  
  <style scoped>
  .product-form {
    max-width: 640px;
    margin: 0 auto;
    padding: 32px;
  }
  
  .title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 32px;
  }
  
  .form-label {
    display: block;
    margin-bottom: 20px;
    font-weight: 500;
    font-size: 14px;
  }
  
  input,
  textarea {
    width: 100%;
    padding: 10px;
    margin-top: 8px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 14px;
  }
  
  .upload-box {
    margin-top: 8px;
    width: 100%;
    height: 200px;
    border: 2px dashed #aaa;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #888;
    cursor: pointer;
    background: #fafafa;
    transition: border 0.2s;
  }
  
  .upload-box:hover {
    border-color: #4a90e2;
  }
  
  .upload-box img {
    max-height: 100%;
    object-fit: contain;
  }
  
  .upload-multi {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 8px;
  }
  
  .sub-preview {
    width: 100px;
    position: relative;
    border: 1px solid #ddd;
    border-radius: 6px;
    overflow: hidden;
  }
  
  .sub-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .sub-preview button {
    position: absolute;
    top: 4px;
    right: 4px;
    background: rgba(255, 0, 0, 0.8);
    color: white;
    border: none;
    border-radius: 50%;
    width: 22px;
    height: 22px;
    font-size: 12px;
    cursor: pointer;
  }
  
  .add-btn {
    width: 100px;
    height: 100px;
    border: 2px dashed #ccc;
    border-radius: 6px;
    background: #f3f4f6;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  
  .add-btn:disabled {
    background: #eee;
    cursor: not-allowed;
  }
  
  .submit-btn {
    margin-top: 24px;
    padding: 12px;
    width: 100%;
    background: #1f4ebb;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    cursor: pointer;
    transition: background 0.2s;
  }
  
  .submit-btn:hover {
    background: #163c95;
  }
  </style>