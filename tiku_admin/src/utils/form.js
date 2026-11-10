export function cloneData(data) {
  return JSON.parse(JSON.stringify(data))
}

export async function validateForm(formRef) {
  if (!formRef?.value) {
    return false
  }
  return formRef.value.validate().catch(() => false)
}

export function resetFormModel(form, initialValue) {
  Object.assign(form, cloneData(initialValue))
}

export function patchFormModel(form, payload, initialValue) {
  resetFormModel(form, initialValue)
  Object.assign(form, cloneData(payload))
}

export function buildPageParams(filters, pageNum, pageSize) {
  return {
    ...cloneData(filters),
    pageNum,
    pageSize,
  }
}
