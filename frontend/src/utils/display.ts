export const ticketStatusText = (status: number | string) => {
  const value = Number(status)
  return (
    {
      0: '待处理',
      1: '处理中',
      2: '已解决',
      3: '已关闭',
    }[value] || `未知(${status})`
  )
}

export const orderStatusText = (status: number | string) => {
  const value = Number(status)
  return (
    {
      '-1': '已取消',
      0: '待付款',
      1: '待发货',
      2: '已发货',
      3: '已签收',
      4: '交易成功',
    }[value] || `未知(${status})`
  )
}

export const userStatusText = (status: number | string) =>
  Number(status) === 1 ? '启用' : '禁用'

export const vipLevelText = (level: number | string) => {
  const value = Number(level)
  if (value <= 0) return '普通用户'
  if (value === 1) return '青铜'
  if (value === 2) return '白银'
  if (value === 3) return '黄金'
  if (value === 4) return '铂金'
  if (value === 5) return '钻石'
  return `至尊VIP Lv.${value}`
}
