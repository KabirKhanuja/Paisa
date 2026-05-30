const admin = require('firebase-admin')
const serviceAccount = require('./serviceAccount.json')

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
})

const db = admin.firestore()

async function seed() {
  // 1. Creating user
  const userRef = db.collection('users').doc('testUser001')
  await userRef.set({
    name: 'Aryan',
    email: 'aryan@paisa.com',
    bank: 'Axis',
    startingBalance: 40000,
    currency: 'INR',
    createdAt: admin.firestore.Timestamp.now()
  })

  // 2. Transactions
  await userRef.collection('transactions').doc().set({
    amount: 60000,
    type: 'credit',
    category: 'Income',
    note: 'Salary',
    source: 'manual',
    date: admin.firestore.Timestamp.now(),
    balanceAfter: 60000
  })
  await userRef.collection('transactions').doc().set({
    amount: 340,
    type: 'debit',
    category: 'Food',
    note: 'Swiggy',
    source: 'auto',
    date: admin.firestore.Timestamp.now(),
    balanceAfter: 59660
  })
  await userRef.collection('transactions').doc().set({
    amount: 2251,
    type: 'debit',
    category: 'Bills',
    note: 'Claude AI',
    source: 'auto',
    date: admin.firestore.Timestamp.now(),
    balanceAfter: 57409
  })

  // 3. Daily snapshot
  await userRef.collection('dailySnapshots').doc('2026-05-31').set({
    date: '2026-05-31',
    openingBalance: 40000,
    closingBalance: 43200,
    totalCredit: 60000,
    totalDebit: 16800,
    transactionCount: 5
  })

  // 4. Budget
  await userRef.collection('budget').doc('2026-05').set({
    salary: 40000,
    spendingCap: 10000,
    investmentTarget: 30000,
    flexBudget: 2349,
    fixedExpenses: [
      { name: 'Claude AI', amount: 2251, icon: 'robot' },
      { name: 'Music', amount: 4800, icon: 'music' },
      { name: 'Petrol', amount: 600, icon: 'car' }
    ]
  })

  // 5. Monthly report
  await userRef.collection('monthlyReports').doc('2026-05').set({
    month: '2026-05',
    totalCredit: 60000,
    totalDebit: 16800,
    costliestDay: '2026-05-16',
    costliestDayAmount: 3200,
    avgDailySpend: 730,
    generatedAt: admin.firestore.Timestamp.now(),
    byCategory: {
      Food: 7644,
      Transport: 4008,
      Bills: 3276,
      Friends: 2184,
      Other: 1092
    }
  })

  console.log('Firestore seeded successfully!')
  process.exit(0)
}

seed().catch(err => {
  console.error('Error:', err)
  process.exit(1)
})